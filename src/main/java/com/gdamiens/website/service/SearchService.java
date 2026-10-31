package com.gdamiens.website.service;

import com.gdamiens.website.client.GeoplateformeClient;
import com.gdamiens.website.client.NavitiaClient;
import com.gdamiens.website.client.PhotonClient;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.PlaceResult;
import com.gdamiens.website.controller.object.v2.SearchResult;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.StopAreaRow;
import com.gdamiens.website.utils.Geo;
import com.gdamiens.website.utils.TtlCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Map.entry;

/**
 * Unified search, without Navitia whenever possible (its quota is for the journeys): lines from the GTFS by name, stop
 * areas from the GTFS ({@link StopSearch}), addresses from the IGN Géoplateforme, places from OpenStreetMap through
 * Photon (shops, cafés, cinemas, chains: the IGN only knows public places), the IGN's when Photon fails, and the
 * closest of a kind for a word such as "pharmacie". Navitia {@code places} only answers when both fail or nothing else
 * found anything. Also the address of a position (reverse geocoding, Géoplateforme).
 */
@Service
public class SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    private static final String NAVITIA_STOP_AREA_PREFIX = "stop_area:";

    private static final String STOP_AREA = "stop_area";

    private static final String ADDRESS = "address";

    private static final String POI = "poi";

    private static final List<String> PLACE_TYPES = List.of(STOP_AREA, ADDRESS, POI);

    /** Départements of Île-de-France: the Géoplateforme covers France */
    private static final Set<String> DEPARTMENTS = Set.of("75", "77", "78", "91", "92", "93", "94", "95");

    /** Around Île-de-France, for the places without postcode */
    private static final double MIN_LAT = 48.1;

    private static final double MAX_LAT = 49.25;

    private static final double MIN_LON = 1.4;

    private static final double MAX_LON = 3.6;

    /** A reverse geocoded address farther than this is not "here" */
    private static final double REVERSE_MAX_METERS = 250;

    /** OpenStreetMap's keys of what is no place to go to: streets, towns, borders, rivers, and stations (the GTFS has them) */
    private static final Set<String> NOT_PLACES = Set.of("highway", "place", "railway", "public_transport", "boundary", "waterway");

    /** The kinds of places asked for by a word ("pharmacie", "boulangeries"): the closest ones, from OpenStreetMap's tags */
    static final Map<String, List<String>> CATEGORIES = Map.ofEntries(
        entry("pharmacie", List.of("amenity:pharmacy")),
        entry("boulangerie", List.of("shop:bakery")),
        entry("supermarche", List.of("shop:supermarket")),
        entry("epicerie", List.of("shop:convenience")),
        entry("cafe", List.of("amenity:cafe")),
        entry("restaurant", List.of("amenity:restaurant", "amenity:fast_food")),
        entry("bar", List.of("amenity:bar", "amenity:pub")),
        entry("cinema", List.of("amenity:cinema")),
        entry("theatre", List.of("amenity:theatre")),
        entry("musee", List.of("tourism:museum")),
        entry("hotel", List.of("tourism:hotel")),
        entry("banque", List.of("amenity:bank")),
        entry("distributeur", List.of("amenity:atm")),
        entry("poste", List.of("amenity:post_office")),
        entry("toilettes", List.of("amenity:toilets")),
        entry("hopital", List.of("amenity:hospital")),
        entry("hopitaux", List.of("amenity:hospital")),
        entry("urgences", List.of("amenity:hospital")),
        entry("medecin", List.of("amenity:doctors")),
        entry("dentiste", List.of("amenity:dentist")),
        entry("veterinaire", List.of("amenity:veterinary")),
        entry("parking", List.of("amenity:parking")),
        entry("station service", List.of("amenity:fuel")),
        entry("parc", List.of("leisure:park")),
        entry("jardin", List.of("leisure:garden", "leisure:park")),
        entry("piscine", List.of("leisure:swimming_pool")),
        entry("salle de sport", List.of("leisure:fitness_centre")),
        entry("bibliotheque", List.of("amenity:library")),
        entry("librairie", List.of("shop:books")),
        entry("fleuriste", List.of("shop:florist")),
        entry("boucherie", List.of("shop:butcher")),
        entry("tabac", List.of("shop:tobacco")),
        entry("pressing", List.of("shop:dry_cleaning")),
        entry("laverie", List.of("shop:laundry")),
        entry("marche", List.of("amenity:marketplace")),
        entry("mairie", List.of("amenity:townhall")),
        entry("commissariat", List.of("amenity:police")),
        entry("police", List.of("amenity:police")));

    /** At most this long a search waits for Photon */
    private static final Duration PHOTON_WAIT = Duration.ofMillis(1500);

    /** How far the closest places of a kind are looked for (km) */
    private static final double CLOSEST_RADIUS_KM = 2;

    /** Optional mode word then the line name: {@code rer b}, {@code métro 13}, {@code m13}, {@code bus 38}, {@code 7bis} */
    private static final Pattern LINE_QUERY = Pattern.compile("^(ligne|metro|m|rer|bus|tram|tramway|noctilien|train|transilien)?\\s*([a-z]?\\d*[a-z]*)$");

    private static final Map<String, List<TransportMode>> MODE_WORDS = Map.of(
        "metro", List.of(TransportMode.METRO),
        "m", List.of(TransportMode.METRO),
        "rer", List.of(TransportMode.RER),
        "bus", List.of(TransportMode.BUS, TransportMode.NOCTILIEN),
        "tram", List.of(TransportMode.TRAM),
        "tramway", List.of(TransportMode.TRAM),
        "noctilien", List.of(TransportMode.NOCTILIEN),
        "train", List.of(TransportMode.TRANSILIEN, TransportMode.TER),
        "transilien", List.of(TransportMode.TRANSILIEN));

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final StopSearch stopSearch;

    private final GeoplateformeClient geoplateformeClient;

    private final PhotonClient photonClient;

    private final NavitiaClient navitiaClient;

    /** Photon asked while the Géoplateforme is: the search waits for the slower of the two, not for both in turn */
    private final ExecutorService photonExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /** Per text and area (about 1 km), or per kind and area (about 200 m) for the closest */
    private final TtlCache<String, List<PlaceResult>> photonCache = new TtlCache<>(Duration.ofMinutes(10), 5000);

    /** Per text and area (about 1 km): typing the same address again, or near each other */
    private final TtlCache<String, List<PlaceResult>> geoCache = new TtlCache<>(Duration.ofMinutes(10), 5000);

    /** Per position (about 10 m) */
    private final TtlCache<String, Optional<PlaceResult>> reverseCache = new TtlCache<>(Duration.ofHours(1), 5000);

    private final TtlCache<String, List<NavitiaPlace>> navitiaCache = new TtlCache<>(Duration.ofMinutes(10), 5000);

    /**
     * A place found by Navitia
     *
     * @param type     Navitia {@code embedded_type}: {@code stop_area}, {@code address} or {@code poi}
     * @param lat      null when Navitia gives no coordinates (stop areas are read from the GTFS)
     */
    private record NavitiaPlace(String type, String id, String name, Double lat, Double lon) {
    }

    public SearchService(NetworkService networkService, NetworkRepository networkRepository, StopSearch stopSearch,
                         GeoplateformeClient geoplateformeClient, PhotonClient photonClient, NavitiaClient navitiaClient) {
        this.networkService = networkService;
        this.networkRepository = networkRepository;
        this.stopSearch = stopSearch;
        this.geoplateformeClient = geoplateformeClient;
        this.photonClient = photonClient;
        this.navitiaClient = navitiaClient;
    }

    /**
     * @param lat the caller's position (null when unknown): closer stop areas, addresses and places first
     */
    public SearchResult search(String query, int limit, Double lat, Double lon) {
        String normalized = normalize(query);
        if (normalized.isEmpty()) {
            return new SearchResult(List.of(), List.of());
        }

        List<LineSummary> lines = searchLines(normalized);
        // A kind of place near the caller ("pharmacie"): the closest ones
        List<String> tags = lat == null || lon == null ? null : categoryTags(normalized);
        CompletableFuture<List<PlaceResult>> osm = CompletableFuture.supplyAsync(
            () -> tags != null ? closestPlaces(normalized, tags, lat, lon, limit) : osmPlaces(normalized, limit, lat, lon), photonExecutor);
        List<PlaceResult> stops = stopSearch.search(normalized, lat, lon, limit);
        List<PlaceResult> geo = geoPlaces(normalized, limit, lat, lon);
        // A slow Photon (a public instance) is not waited for: the Géoplateforme's places meanwhile, its answer cached for the next keystroke
        List<PlaceResult> places = osm.completeOnTimeout(null, PHOTON_WAIT.toMillis(), TimeUnit.MILLISECONDS).join();
        if (tags != null && places != null && !places.isEmpty()) {
            // Then the stop areas named so, typed on the way to them ("Hôtel de Ville", "Mairie de Montreuil"): half at most
            List<PlaceResult> namedStops = stops.stream().filter(stop -> namedAfter(normalized, stop)).limit(limit / 2).toList();
            return new SearchResult(lines, Stream.concat(places.stream().limit(limit - namedStops.size()), namedStops.stream()).toList());
        }
        List<PlaceResult> osmPlaces = tags == null ? places : null;
        List<PlaceResult> others = others(geo, osmPlaces);
        if (geo == null || stops.isEmpty() && others.isEmpty() && lines.isEmpty()) {
            // Navitia's addresses and places instead of the Géoplateforme's, then OpenStreetMap's
            others = Stream.concat(navitiaPlaces(normalized, limit, stops.isEmpty()).stream(),
                Objects.requireNonNullElse(osmPlaces, List.<PlaceResult>of()).stream()).toList();
        }
        return new SearchResult(lines, merge(normalized, stops, others, limit));
    }

    /**
     * The Géoplateforme's addresses and OpenStreetMap's places, one after the other (neither drowns the other); the
     * Géoplateforme's own places when Photon failed or found none
     */
    static List<PlaceResult> others(List<PlaceResult> geo, List<PlaceResult> osm) {
        List<PlaceResult> fromGeo = Objects.requireNonNullElse(geo, List.of());
        List<PlaceResult> addresses = fromGeo.stream().filter(place -> place.type() == PlaceResult.Type.ADDRESS).toList();
        List<PlaceResult> pois = osm != null && !osm.isEmpty() ? osm : fromGeo.stream().filter(place -> place.type() == PlaceResult.Type.POI).toList();
        List<PlaceResult> others = new ArrayList<>();
        for (int i = 0; i < Math.max(addresses.size(), pois.size()); i++) {
            if (i < addresses.size()) {
                others.add(addresses.get(i));
            }
            if (i < pois.size()) {
                others.add(pois.get(i));
            }
        }
        return others;
    }

    /**
     * Stop areas first, addresses first when the query starts with a house number; each keeps half the places when the
     * other has enough. Then a place named after every word typed comes before one only resembling them (a typo away:
     * "fnac" is not "Frachon").
     */
    static List<PlaceResult> merge(String normalized, List<PlaceResult> stops, List<PlaceResult> others, int limit) {
        boolean houseNumber = Character.isDigit(normalized.charAt(0));
        List<PlaceResult> first = houseNumber ? others : stops;
        List<PlaceResult> second = houseNumber ? stops : others;
        int keptForSecond = Math.min(second.size(), limit / 2);
        List<PlaceResult> merged = new ArrayList<>(first.stream().limit(limit - keptForSecond).toList());
        second.stream().limit(limit - merged.size()).forEach(merged::add);
        // Stable: the order above among the ones named so, and among the others
        merged.sort(Comparator.comparing(place -> !namedAfter(normalized, place)));
        return merged;
    }

    /** Every word typed starts a word of the place's name (its town in brackets aside) */
    static boolean namedAfter(String normalized, PlaceResult place) {
        List<String> words = List.of(normalize(place.name().replaceAll("\\s*\\([^)]*\\)$", "")).split("[ '-]+"));
        return Arrays.stream(normalized.split("[ '-]+")).allMatch(typed -> words.stream().anyMatch(word -> word.startsWith(typed)));
    }

    /** OpenStreetMap's tags of a kind of place asked for by its word, singular or plural; null for any other text */
    static List<String> categoryTags(String normalized) {
        List<String> tags = CATEGORIES.get(normalized);
        if (tags == null && (normalized.endsWith("s") || normalized.endsWith("x"))) {
            tags = CATEGORIES.get(normalized.substring(0, normalized.length() - 1));
        }
        return tags;
    }

    List<LineSummary> searchLines(String normalized) {
        // "m13" is the metro 13 but "m" alone could be a line name, so the whole query is also tried as a name
        Matcher matcher = LINE_QUERY.matcher(normalized);
        String name = matcher.matches() ? matcher.group(2) : "";
        List<TransportMode> modes = name.isEmpty() ? null : Optional.ofNullable(matcher.group(1)).map(MODE_WORDS::get).orElse(null);
        String wholeName = normalized.replace(" ", "");

        return networkService.getLines().values().stream()
            .filter(line -> line.name() != null)
            .filter(line -> {
                String lineName = normalize(line.name());
                return (!name.isEmpty() && lineName.equals(name) && (modes == null || modes.contains(line.mode())))
                    || lineName.equals(wholeName);
            })
            .sorted(NetworkService.LINE_ORDER)
            .limit(10)
            .toList();
    }

    /** Géoplateforme addresses and places, null when it failed (too short a text: none) */
    private List<PlaceResult> geoPlaces(String normalized, int limit, Double lat, Double lon) {
        if (normalized.length() < 3) {
            return List.of();
        }
        // About 1 km: enough to rank, and the position stays vague
        Double areaLat = lat == null ? null : Math.round(lat * 100) / 100.0;
        Double areaLon = lon == null ? null : Math.round(lon * 100) / 100.0;
        try {
            return geoCache.get(normalized + "|" + limit + "|" + areaLat + "|" + areaLon,
                k -> toGeoPlaces(geoplateformeClient.search(normalized, areaLat, areaLon, limit + 5)));
        } catch (RuntimeException e) {
            LOGGER.warn("Géoplateforme search failed for '{}': {}", normalized, e.getMessage());
            return null;
        }
    }

    /**
     * Places of a Géoplateforme answer in Île-de-France (by postcode, else by position). Stations and stops are left out (the GTFS has them), and the
     * same name only comes once (a town is both an address and a place).
     */
    static List<PlaceResult> toGeoPlaces(JsonNode collection) {
        List<PlaceResult> places = new ArrayList<>();
        if (collection == null) {
            return places;
        }
        Set<String> names = new HashSet<>();
        for (JsonNode feature : collection.path("features").values()) {
            double lon = feature.path("geometry").path("coordinates").path(0).asDouble(Double.NaN);
            double lat = feature.path("geometry").path("coordinates").path(1).asDouble(Double.NaN);
            if (!(lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON)) {
                continue;
            }
            JsonNode properties = feature.path("properties");
            List<String> postcodes = strings(properties.path("postcode"));
            if (!postcodes.isEmpty() && postcodes.stream().noneMatch(postcode -> postcode.length() >= 2 && DEPARTMENTS.contains(postcode.substring(0, 2)))) {
                continue;
            }
            PlaceResult place;
            if (POI.equals(properties.path("_type").asString(""))) {
                String name = StringUtils.trimToNull(properties.path("toponym").asString(null));
                boolean stop = strings(properties.path("category")).stream()
                    .anyMatch(category -> category.startsWith("station") || category.startsWith("gare") || category.startsWith("arrêt"));
                if (name == null || stop) {
                    continue;
                }
                // "Paris 12e", like the stop areas' towns
                String city = strings(properties.path("city")).stream().findFirst().map(town -> town.replace(" Arrondissement", "")).orElse(null);
                place = new PlaceResult(PlaceResult.Type.POI, "poi:" + properties.path("extrafields").path("cleabs").asString(name),
                    city == null || city.equalsIgnoreCase(name) ? name : name + " (" + city + ")", lat, lon, null);
            } else {
                String label = StringUtils.trimToNull(properties.path("label").asString(null));
                if (label == null) {
                    continue;
                }
                place = new PlaceResult(PlaceResult.Type.ADDRESS, "address:" + properties.path("id").asString(label), label, lat, lon, null);
            }
            if (names.add(normalize(place.name()))) {
                places.add(place);
            }
        }
        return places;
    }

    /** OpenStreetMap's places matching a text (Photon), null when it failed (too short a text: none) */
    private List<PlaceResult> osmPlaces(String normalized, int limit, Double lat, Double lon) {
        if (normalized.length() < 3) {
            return List.of();
        }
        // About 1 km, as for the Géoplateforme
        Double areaLat = lat == null ? null : Math.round(lat * 100) / 100.0;
        Double areaLon = lon == null ? null : Math.round(lon * 100) / 100.0;
        try {
            return photonCache.get("search|" + normalized + "|" + limit + "|" + areaLat + "|" + areaLon,
                k -> toOsmPlaces(photonClient.search(normalized, areaLat, areaLon, limit + 5)));
        } catch (RuntimeException e) {
            LOGGER.warn("Photon search failed for '{}': {}", normalized, e.getMessage());
            return null;
        }
    }

    /** The closest places of a kind (Photon), null when it failed */
    private List<PlaceResult> closestPlaces(String normalized, List<String> tags, double lat, double lon, int limit) {
        // About 200 m: the closest stay the closest, and the position stays vague
        double areaLat = Math.round(lat * 500) / 500.0;
        double areaLon = Math.round(lon * 500) / 500.0;
        try {
            return photonCache.get("closest|" + String.join(",", tags) + "|" + limit + "|" + areaLat + "|" + areaLon,
                k -> toOsmPlaces(photonClient.closest(tags, areaLat, areaLon, limit, CLOSEST_RADIUS_KM)));
        } catch (RuntimeException e) {
            LOGGER.warn("Photon closest '{}' failed: {}", normalized, e.getMessage());
            return null;
        }
    }

    /**
     * OpenStreetMap's places of a Photon answer: named, in Île-de-France, no street, town or station (the GTFS has
     * them), each once (a shop is often both a point and a building); with their street and number, and their kind
     */
    static List<PlaceResult> toOsmPlaces(JsonNode collection) {
        List<PlaceResult> places = new ArrayList<>();
        if (collection == null) {
            return places;
        }
        for (JsonNode feature : collection.path("features").values()) {
            double lon = feature.path("geometry").path("coordinates").path(0).asDouble(Double.NaN);
            double lat = feature.path("geometry").path("coordinates").path(1).asDouble(Double.NaN);
            if (!(lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON)) {
                continue;
            }
            JsonNode properties = feature.path("properties");
            String name = StringUtils.trimToNull(properties.path("name").asString(null));
            String key = properties.path("osm_key").asString("");
            String value = StringUtils.trimToNull(properties.path("osm_value").asString(null));
            if (name == null || NOT_PLACES.contains(key) || "bus_station".equals(value)) {
                continue;
            }
            String postcode = StringUtils.trimToNull(properties.path("postcode").asString(null));
            if (postcode != null && (postcode.length() < 2 || !DEPARTMENTS.contains(postcode.substring(0, 2)))) {
                continue;
            }
            String town = town(postcode, StringUtils.trimToNull(properties.path("city").asString(null)));
            String label = town == null || town.equalsIgnoreCase(name) ? name : name + " (" + town + ")";
            // Within 100 m, the same name is the same place (a shop and its building)
            if (places.stream().anyMatch(place -> place.name().equals(label) && Geo.meters(lat, lon, place.lat(), place.lon()) < 100)) {
                continue;
            }
            String street = StringUtils.trimToNull(properties.path("street").asString(null));
            String number = StringUtils.trimToNull(properties.path("housenumber").asString(null));
            places.add(new PlaceResult(PlaceResult.Type.POI,
                "osm:" + properties.path("osm_type").asString("") + properties.path("osm_id").asString(""),
                label, lat, lon, null,
                street == null ? null : number == null ? street : number + " " + street, value));
        }
        return places;
    }

    /** "Paris 1er", "Paris 16e" from a Paris postcode, like the stop areas' towns; else the city */
    static String town(String postcode, String city) {
        if (postcode != null && postcode.matches("75\\d{3}")) {
            int arrondissement = Integer.parseInt(postcode.substring(3));
            if (arrondissement >= 1 && arrondissement <= 20) {
                return "Paris " + (arrondissement == 1 ? "1er" : arrondissement + "e");
            }
        }
        return city;
    }

    /** A text, or a list of texts (the Géoplateforme gives both) */
    private static List<String> strings(JsonNode value) {
        if (value.isArray()) {
            return value.values().stream().map(item -> item.asString("")).filter(item -> !item.isBlank()).toList();
        }
        String text = StringUtils.trimToNull(value.asString(null));
        return text == null ? List.of() : List.of(text);
    }

    /**
     * The address at a position (map long press, start of a journey): the position itself, named after the closest
     * address within {@value #REVERSE_MAX_METERS} m. Empty when there is none or the Géoplateforme fails.
     */
    public Optional<PlaceResult> reverse(double lat, double lon) {
        double roundedLat = Math.round(lat * 10_000) / 10_000.0;
        double roundedLon = Math.round(lon * 10_000) / 10_000.0;
        try {
            return reverseCache.get(roundedLat + "," + roundedLon, k -> {
                JsonNode feature = geoplateformeClient.reverse(roundedLat, roundedLon).path("features").path(0);
                String label = StringUtils.trimToNull(feature.path("properties").path("label").asString(null));
                double distance = feature.path("properties").path("distance").asDouble(Double.MAX_VALUE);
                if (label == null || distance > REVERSE_MAX_METERS) {
                    return Optional.empty();
                }
                return Optional.of(new PlaceResult(PlaceResult.Type.ADDRESS, "address:" + feature.path("properties").path("id").asString(label),
                    label, roundedLat, roundedLon, null));
            });
        } catch (RuntimeException e) {
            LOGGER.warn("Géoplateforme reverse geocoding failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Navitia {@code places}, the last resort: with stop areas when the GTFS found none */
    private List<PlaceResult> navitiaPlaces(String normalized, int limit, boolean withStopAreas) {
        List<NavitiaPlace> places;
        try {
            List<String> types = withStopAreas ? PLACE_TYPES : List.of(ADDRESS, POI);
            places = navitiaCache.get(normalized + "|" + limit + "|" + withStopAreas,
                k -> toPlaces(navitiaClient.getPlaces(normalized, types, limit)));
        } catch (RuntimeException e) {
            LOGGER.warn("Navitia places search failed for '{}': {}", normalized, e.getMessage());
            return List.of();
        }

        List<String> stopAreaIds = places.stream()
            .filter(place -> STOP_AREA.equals(place.type()) && place.id() != null)
            .map(place -> StringUtils.removeStart(place.id(), NAVITIA_STOP_AREA_PREFIX))
            .toList();
        Map<String, StopAreaRow> stopAreas = networkRepository.findStopAreas(stopAreaIds);
        Map<String, List<String>> routeIds = networkRepository.findRouteIdsByStopAreas(stopAreas.keySet());

        List<PlaceResult> results = new ArrayList<>();
        for (NavitiaPlace place : places) {
            PlaceResult result = switch (place.type()) {
                case STOP_AREA -> Optional.ofNullable(stopAreas.get(StringUtils.removeStart(place.id(), NAVITIA_STOP_AREA_PREFIX)))
                    .map(stop -> new PlaceResult(PlaceResult.Type.STOP_AREA, stop.id(), place.name(), stop.lat(), stop.lon(),
                        networkService.toLineSummaries(routeIds.getOrDefault(stop.id(), List.of()))))
                    .orElse(null);
                case ADDRESS -> toPlace(PlaceResult.Type.ADDRESS, place);
                case POI -> toPlace(PlaceResult.Type.POI, place);
                default -> null;
            };
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    /** Navitia {@code places} response: coordinates are strings, under the object of the place's type */
    private static List<NavitiaPlace> toPlaces(JsonNode response) {
        if (response == null) {
            return List.of();
        }
        return response.path("places").values().stream()
            .map(place -> {
                String type = place.path("embedded_type").asString("");
                JsonNode coord = place.path(type).path("coord");
                return new NavitiaPlace(type, place.path("id").asString(null), place.path("name").asString(null),
                    coordinate(coord.path("lat")), coordinate(coord.path("lon")));
            })
            .toList();
    }

    private static Double coordinate(JsonNode value) {
        double coordinate = value.asDouble(Double.NaN);
        return Double.isNaN(coordinate) ? null : coordinate;
    }

    private static PlaceResult toPlace(PlaceResult.Type type, NavitiaPlace place) {
        if (place.id() == null || place.lat() == null || place.lon() == null) {
            return null;
        }
        return new PlaceResult(type, place.id(), place.name(), place.lat(), place.lon(), null);
    }

    /** Lower case, no accents, single spaces */
    static String normalize(String value) {
        return Objects.requireNonNullElse(StringUtils.stripAccents(value), "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9' -]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
