package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.PlaceResult;
import com.gdamiens.website.controller.object.v2.SearchResult;
import com.gdamiens.website.idfm.navitia.Coord;
import com.gdamiens.website.idfm.navitia.EmbeddedTypeEnum;
import com.gdamiens.website.idfm.navitia.Place;
import com.gdamiens.website.idfm.navitia.Places;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.StopAreaRow;
import com.gdamiens.website.utils.TtlCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified search: lines from the GTFS by name, stop areas / addresses / points of interest from Navitia (which knows
 * the towns). Navitia stop areas ({@code stop_area:IDFM:71264}) are the GTFS stop areas ({@code IDFM:71264}).
 */
@Service
public class SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    private static final String NAVITIA_STOP_AREA_PREFIX = "stop_area:";

    private static final List<String> PLACE_TYPES = List.of("stop_area", "address", "poi");

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

    private final IDFMNavitiaService idfmNavitiaService;

    private final TtlCache<String, List<Place>> navitiaCache = new TtlCache<>(Duration.ofMinutes(10), 5000);

    public SearchService(NetworkService networkService, NetworkRepository networkRepository, IDFMNavitiaService idfmNavitiaService) {
        this.networkService = networkService;
        this.networkRepository = networkRepository;
        this.idfmNavitiaService = idfmNavitiaService;
    }

    public SearchResult search(String query, int limit) {
        String normalized = normalize(query);
        if (normalized.isEmpty()) {
            return new SearchResult(List.of(), List.of());
        }

        return new SearchResult(searchLines(normalized), searchPlaces(normalized, limit));
    }

    List<LineSummary> searchLines(String normalized) {
        Matcher matcher = LINE_QUERY.matcher(normalized);
        if (!matcher.matches() || matcher.group(2).isEmpty()) {
            return List.of();
        }

        String name = matcher.group(2);
        List<TransportMode> modes = Optional.ofNullable(matcher.group(1)).map(MODE_WORDS::get).orElse(null);

        // "m13" is the metro 13 but "m" alone could be a line name, so also try the whole query as a name
        return networkService.getLines().values().stream()
            .filter(line -> line.name() != null)
            .filter(line -> {
                String lineName = normalize(line.name());
                return (lineName.equals(name) && (modes == null || modes.contains(line.mode())))
                    || lineName.equals(normalized.replace(" ", ""));
            })
            .sorted(NetworkService.LINE_ORDER)
            .limit(10)
            .toList();
    }

    private List<PlaceResult> searchPlaces(String normalized, int limit) {
        List<Place> places;
        try {
            places = navitiaCache.get(normalized + "|" + limit, k -> Optional.ofNullable(idfmNavitiaService.getPlaces(normalized, PLACE_TYPES, limit))
                .map(Places::getPlaces)
                .orElse(List.of()));
        } catch (RuntimeException e) {
            LOGGER.warn("Navitia places search failed for '{}': {}", normalized, e.getMessage());
            return List.of();
        }

        List<String> stopAreaIds = places.stream()
            .filter(place -> place.getEmbeddedType() == EmbeddedTypeEnum.STOP_AREA && place.getId() != null)
            .map(place -> StringUtils.removeStart(place.getId(), NAVITIA_STOP_AREA_PREFIX))
            .toList();
        Map<String, StopAreaRow> stopAreas = networkRepository.findStopAreas(stopAreaIds);
        Map<String, List<String>> routeIds = networkRepository.findRouteIdsByStopAreas(stopAreas.keySet());

        List<PlaceResult> results = new ArrayList<>();
        for (Place place : places) {
            PlaceResult result = switch (place.getEmbeddedType()) {
                case STOP_AREA -> Optional.ofNullable(stopAreas.get(StringUtils.removeStart(place.getId(), NAVITIA_STOP_AREA_PREFIX)))
                    .map(stop -> new PlaceResult(PlaceResult.Type.STOP_AREA, stop.id(), place.getName(), stop.lat(), stop.lon(),
                        networkService.toLineSummaries(routeIds.getOrDefault(stop.id(), List.of()))))
                    .orElse(null);
                case ADDRESS -> toPlace(PlaceResult.Type.ADDRESS, place, place.getAddress() == null ? null : place.getAddress().getCoord());
                case POI -> toPlace(PlaceResult.Type.POI, place, place.getPoi() == null ? null : place.getPoi().getCoord());
                case null, default -> null;
            };
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    private static PlaceResult toPlace(PlaceResult.Type type, Place place, Coord coord) {
        if (coord == null || place.getId() == null) {
            return null;
        }
        try {
            return new PlaceResult(type, place.getId(), place.getName(), Double.parseDouble(coord.getLat()), Double.parseDouble(coord.getLon()), null);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
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
