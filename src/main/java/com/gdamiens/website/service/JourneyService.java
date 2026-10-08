package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.BikeStation;
import com.gdamiens.website.controller.object.v2.JourneyOption;
import com.gdamiens.website.controller.object.v2.JourneyPlan;
import com.gdamiens.website.controller.object.v2.JourneyPlan.PageCursor;
import com.gdamiens.website.controller.object.v2.JourneyPoint;
import com.gdamiens.website.controller.object.v2.JourneySection;
import com.gdamiens.website.controller.object.v2.JourneySection.Kind;
import com.gdamiens.website.controller.object.v2.JourneyStop;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.controller.object.v2.WalkStep;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.utils.GeoJson;
import com.gdamiens.website.utils.TtlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Journey planning (Navitia) turned into the compact v2 model, with GTFS line ids and stop areas.
 */
@Service
public class JourneyService {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private static final DateTimeFormatter NAVITIA_DATETIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static final Pattern COORDINATES = Pattern.compile("^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$");

    private static final Pattern STOP_AREA = Pattern.compile("^IDFM:\\d+$");

    /** Navitia physical modes of each GTFS transport mode */
    private static final Map<TransportMode, List<String>> PHYSICAL_MODES = Map.of(
        TransportMode.METRO, List.of("physical_mode:Metro", "physical_mode:RailShuttle"),
        TransportMode.RER, List.of("physical_mode:RapidTransit"),
        TransportMode.TRANSILIEN, List.of("physical_mode:LocalTrain"),
        TransportMode.TER, List.of("physical_mode:Train"),
        TransportMode.TRAM, List.of("physical_mode:Tramway", "physical_mode:Funicular", "physical_mode:SuspendedCableCar"),
        TransportMode.BUS, List.of("physical_mode:Bus"),
        TransportMode.NOCTILIEN, List.of("physical_mode:Bus"));

    public enum WalkingSpeed {
        SLOW(0.9), NORMAL(null), FAST(1.5);

        private final Double metersPerSecond;

        WalkingSpeed(Double metersPerSecond) {
            this.metersPerSecond = metersPerSecond;
        }
    }

    /**
     * @param from     {@code lat,lon} or a stop area id ({@code IDFM:71264})
     * @param datetime departure (or arrival when {@code arriveBy}) time, now when null
     * @param modes    public transport modes allowed, all when empty
     */
    public record JourneyQuery(String from, String to, Instant datetime, boolean arriveBy, Collection<TransportMode> modes,
                               boolean wheelchair, WalkingSpeed walkingSpeed, Integer maxTransfers, boolean bikeShare) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JourneyService.class);

    /** Time the Vélib option may take beyond the public transport ones */
    private static final Duration BIKE_SHARE_BUDGET = Duration.ofSeconds(4);

    /** Vélib stations looked for around the start and the end */
    private static final int BIKE_STATION_RADIUS = 600;

    /** Meters per second on a Vélib, stops at lights included (about 15 km/h) */
    private static final double BIKE_SPEED = 4.2;

    /** Taking or returning a bike */
    private static final Duration BIKE_HANDLING = Duration.ofMinutes(1);

    /** Time the GTFS paths may add to a journey request; slower ones are used by the next requests */
    private static final Duration SHAPE_BUDGET = Duration.ofMillis(300);

    private final IDFMNavitiaService idfmNavitiaService;

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final BikeService bikeService;

    private final TtlCache<String, RidePath> rideCache = new TtlCache<>(Duration.ofHours(24), 5000);

    /** With the caller's authentication: ApiQuota counts guests apart (the Vélib option calls Navitia) */
    private final ExecutorService shapeExecutor = new DelegatingSecurityContextExecutorService(Executors.newVirtualThreadPerTaskExecutor());

    private final TtlCache<String, Optional<List<double[]>>> shapeCache = new TtlCache<>(Duration.ofHours(6), 20000);

    /** A new GTFS replaced the previous one: its shapes have other ids */
    @EventListener(GtfsImportedEvent.class)
    public void onGtfsImported() {
        shapeCache.invalidateAll();
    }

    public JourneyService(IDFMNavitiaService idfmNavitiaService, NetworkService networkService, NetworkRepository networkRepository,
                          BikeService bikeService) {
        this.idfmNavitiaService = idfmNavitiaService;
        this.networkService = networkService;
        this.networkRepository = networkRepository;
        this.bikeService = bikeService;
    }

    public JourneyPlan plan(JourneyQuery query) {
        // Navitia has no Vélib stations in Île-de-France: that option is built here, alongside
        CompletableFuture<Optional<JourneyOption>> bikeShare = query.bikeShare()
            ? CompletableFuture.supplyAsync(() -> bikeShareJourney(query), shapeExecutor)
            : CompletableFuture.completedFuture(Optional.empty());
        JsonNode response;
        try {
            response = idfmNavitiaService.planJourneys(
                toNavitiaPlace(query.from()),
                toNavitiaPlace(query.to()),
                query.datetime() == null ? null : NAVITIA_DATETIME.format(query.datetime().atZone(PARIS)),
                query.arriveBy(),
                forbiddenModes(query.modes()),
                query.wheelchair(),
                query.walkingSpeed() == null ? null : query.walkingSpeed().metersPerSecond,
                query.maxTransfers());
        } catch (NavitiaException e) {
            // No solution (e.g. no public transport at night) is not an error for the app
            if (StringUtils.contains(e.getResponseBody(), "no_solution")) {
                return new JourneyPlan(List.of(), null, null);
            }
            if (e.getStatusCode().is4xxClientError()) {
                throw new CustomException("Journey request rejected: unknown or unreachable place", HttpStatus.BAD_REQUEST);
            }
            throw new CustomException("Journey planning unavailable", HttpStatus.BAD_GATEWAY);
        }

        Map<String, LineSummary> lines = networkService.getLines();
        List<JourneyOption> journeys = new ArrayList<>();
        for (JsonNode journey : response.path("journeys").values()) {
            journeys.add(toJourney(journey, lines));
        }
        try {
            bikeShare.get(BIKE_SHARE_BUDGET.toMillis(), TimeUnit.MILLISECONDS).ifPresent(journeys::add);
        } catch (Exception e) {
            LOGGER.warn("Vélib journey unavailable: {}", e.toString());
        }

        PageCursor earlier = null;
        PageCursor later = null;
        for (JsonNode link : response.path("links").values()) {
            switch (link.path("rel").asString("")) {
                case "prev" -> earlier = toCursor(link.path("href").asString(null));
                case "next" -> later = toCursor(link.path("href").asString(null));
                default -> {
                }
            }
        }
        return new JourneyPlan(withGtfsShapes(journeys), earlier, later);
    }

    /**
     * Vélib option: walk to the closest station with a bike, ride to the closest station with a free dock near the
     * end, walk from there. Navitia gives the walking paths; PRIM has no bike routing, so the ride follows the walking
     * path between the stations at {@link #BIKE_SPEED}. Empty when there is no such pair of stations.
     */
    private Optional<JourneyOption> bikeShareJourney(JourneyQuery query) {
        double[] from = coordinates(query.from());
        double[] to = coordinates(query.to());
        if (from == null || to == null) {
            return Optional.empty();
        }
        BikeStation take = bikeService.getNearby(from[0], from[1], BIKE_STATION_RADIUS, 10).stream()
            .filter(station -> station.renting() && station.mechanical() + station.electric() > 0)
            .findFirst().orElse(null);
        BikeStation leave = bikeService.getNearby(to[0], to[1], BIKE_STATION_RADIUS, 10).stream()
            .filter(station -> station.returning() && station.docks() > 0)
            .findFirst().orElse(null);
        if (take == null || leave == null || take.id().equals(leave.id())) {
            return Optional.empty();
        }

        // Walks to and from the stations (600 m at most) estimated from the distance; the ride from Navitia's walking
        // path between the stations, kept a day per pair (PRIM's quota)
        Double chosenSpeed = query.walkingSpeed() == null ? null : query.walkingSpeed().metersPerSecond;
        // Navitia's default walking speed for NORMAL
        double walkingSpeed = chosenSpeed == null ? 1.12 : chosenSpeed;
        JourneyPoint origin = place(query.from(), from);
        JourneyPoint destination = place(query.to(), to);
        RidePath ridePath = rideCache.get(take.id() + "|" + leave.id(), key -> ridePath(take, leave));
        int rideDuration = Math.max(60, (int) Math.round(ridePath.length() / BIKE_SPEED));
        List<JourneySection> parts = List.of(
            walk(origin, stationPoint(take), walkingSpeed),
            new JourneySection(Kind.BIKE, null, null, rideDuration, stationPoint(take), stationPoint(leave), null, null, List.of(),
                List.of(), List.of(), null, null, ridePath.length(), ridePath.shape()),
            walk(stationPoint(leave), destination, walkingSpeed));

        // One after the other, from the time asked (or to it, for an arrival time)
        long total = parts.stream().mapToLong(JourneySection::duration).sum() + 2 * BIKE_HANDLING.toSeconds();
        Instant asked = query.datetime() == null ? Instant.now() : query.datetime();
        Instant cursor = query.arriveBy() ? asked.minusSeconds(total) : asked;
        Instant departure = cursor;
        List<JourneySection> sections = new ArrayList<>();
        int walking = 0;
        int walkingDistance = 0;
        for (int i = 0; i < parts.size(); i++) {
            JourneySection part = parts.get(i);
            boolean bike = part.kind() == Kind.BIKE;
            if (bike) {
                cursor = cursor.plus(BIKE_HANDLING);
            } else {
                walking += part.duration();
                walkingDistance += Optional.ofNullable(part.length()).orElse(0);
            }
            Instant end = cursor.plusSeconds(part.duration());
            sections.add(new JourneySection(part.kind(), cursor, end, part.duration(),
                part.from(), part.to(), null, null, List.of(), List.of(), part.steps(), null, null, part.length(), part.shape()));
            cursor = bike ? end.plus(BIKE_HANDLING) : end;
        }
        return Optional.of(new JourneyOption("bike_share", List.of("bike_share"), departure, cursor,
            (int) Duration.between(departure, cursor).toSeconds(), 0, walking, walkingDistance, 0.0, null, sections));
    }

    /** Path of a Vélib ride between two stations */
    private record RidePath(int length, List<double[]> shape) {
    }

    /** Navitia's walking path between the stations (bike routing is missing from PRIM), else the straight line */
    private RidePath ridePath(BikeStation take, BikeStation leave) {
        try {
            JsonNode journey = idfmNavitiaService.planWalkingPath(take.lon() + ";" + take.lat(), leave.lon() + ";" + leave.lat(), null, null)
                .path("journeys").path(0);
            List<JourneySection> sections = journey.isMissingNode() ? List.of() : toJourney(journey, Map.of()).sections();
            if (!sections.isEmpty()) {
                List<double[]> shape = sections.stream().flatMap(section -> section.shape().stream()).toList();
                int length = sections.stream().mapToInt(section -> Optional.ofNullable(section.length())
                    .orElse((int) Math.round(section.duration() * 1.1))).sum();
                return new RidePath(length, shape);
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Vélib ride path unavailable: {}", e.getMessage());
        }
        return new RidePath(walkingLength(take.lat(), take.lon(), leave.lat(), leave.lon()),
            List.of(new double[]{take.lon(), take.lat()}, new double[]{leave.lon(), leave.lat()}));
    }

    /** Walk on a straight line, a bit longer for the streets (times are set later) */
    private static JourneySection walk(JourneyPoint from, JourneyPoint to, double metersPerSecond) {
        int length = walkingLength(from.lat(), from.lon(), to.lat(), to.lon());
        return new JourneySection(Kind.WALK, null, null, (int) Math.round(length / metersPerSecond), from, to, null, null, List.of(),
            List.of(), List.of(), null, null, length, List.of(new double[]{from.lon(), from.lat()}, new double[]{to.lon(), to.lat()}));
    }

    /** Streets are about 30 % longer than the straight line */
    private static int walkingLength(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return (int) Math.round(Math.sqrt(x * x + y * y) * 6_371_000 * 1.3);
    }

    /** Start or end of a journey: the stop area's name, else a position */
    private JourneyPoint place(String place, double[] coordinates) {
        String name = networkService.getStopAreaSummary(place.trim()).map(StopAreaSummary::name).orElse("Position");
        return new JourneyPoint(name, coordinates[0], coordinates[1], STOP_AREA.matcher(place.trim()).matches() ? place.trim() : null);
    }

    private static JourneyPoint stationPoint(BikeStation station) {
        return new JourneyPoint("Station Vélib " + station.name(), station.lat(), station.lon(), null);
    }

    /** {@code [lat, lon]} of a journey place ({@code lat,lon} or a stop area id), null when unknown */
    private double[] coordinates(String place) {
        String value = place.trim();
        if (COORDINATES.matcher(value).matches()) {
            String[] parts = value.split(",");
            return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        }
        return networkService.getStopAreaSummary(value).map(stop -> new double[]{stop.lat(), stop.lon()}).orElse(null);
    }

    /**
     * Navitia has no path for some trips (e.g. Transilien L direct trains): the section is then drawn as straight
     * segments between the served stops. Those sections get the GTFS path of their line instead, cut between the
     * boarding and alighting stops. Lookups run in parallel, cached, within {@link #SHAPE_BUDGET}: what is not ready
     * keeps Navitia's path and is cached for the next requests.
     */
    private List<JourneyOption> withGtfsShapes(List<JourneyOption> journeys) {
        Map<String, CompletableFuture<Optional<List<double[]>>>> lookups = new LinkedHashMap<>();
        for (JourneyOption journey : journeys) {
            for (JourneySection section : journey.sections()) {
                String key = shapeKey(section);
                if (key != null && !lookups.containsKey(key)) {
                    lookups.put(key, CompletableFuture
                        .supplyAsync(() -> shapeCache.get(key, k -> findGtfsShape(section)), shapeExecutor)
                        .whenComplete((shape, error) -> {
                            if (error != null) {
                                LOGGER.warn("GTFS path lookup failed for {}: {}", key, error.getMessage());
                            }
                        }));
                }
            }
        }
        if (lookups.isEmpty()) {
            return journeys;
        }

        try {
            CompletableFuture.allOf(lookups.values().toArray(CompletableFuture[]::new))
                .get(SHAPE_BUDGET.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // Timeout or failure: use the paths that are ready
        }

        Map<String, List<double[]>> shapes = new HashMap<>();
        lookups.forEach((key, lookup) -> {
            Optional<List<double[]>> shape = lookup.isDone() && !lookup.isCompletedExceptionally() ? lookup.join() : Optional.empty();
            shape.filter(points -> points.size() >= 2).ifPresent(points -> shapes.put(key, points));
        });
        if (shapes.isEmpty()) {
            return journeys;
        }

        return journeys.stream().map(journey -> new JourneyOption(journey.type(), journey.tags(), journey.departure(),
            journey.arrival(), journey.duration(), journey.transfers(), journey.walkingDuration(), journey.walkingDistance(),
            journey.co2(), journey.fare(), journey.sections().stream().map(section -> {
                String key = shapeKey(section);
                List<double[]> shape = key == null ? null : shapes.get(key);
                return shape == null ? section : new JourneySection(section.kind(), section.departure(), section.arrival(),
                    section.duration(), section.from(), section.to(), section.line(), section.headsign(),
                    section.boardingPositions(), section.stops(), section.steps(), section.realtime(), section.delay(),
                    section.length(), shape);
            }).toList())).toList();
    }

    /**
     * @return cache key of a ride whose Navitia path is only its stops (about one point per served stop), null if
     * the path is fine or the line is not in the GTFS
     */
    private static String shapeKey(JourneySection section) {
        if (section.kind() != Kind.TRANSIT || section.line() == null || section.line().id() == null
            || section.from() == null || section.to() == null
            || section.shape().size() > Math.max(section.stops().size(), 2) + 1) {
            return null;
        }
        return "%s|%.4f|%.4f|%.4f|%.4f".formatted(section.line().id(), section.from().lat(), section.from().lon(),
            section.to().lat(), section.to().lon());
    }

    /** Failures are thrown, not cached: the next request tries again */
    private Optional<List<double[]>> findGtfsShape(JourneySection section) {
        return networkRepository.findShapeBetween(IDFMRoute.toRouteId(section.line().id()),
                section.from().lat(), section.from().lon(), section.to().lat(), section.to().lon())
            .map(GeoJson::lineStringCoordinates);
    }

    private JourneyOption toJourney(JsonNode journey, Map<String, LineSummary> lines) {
        List<JourneySection> sections = new ArrayList<>();
        for (JsonNode section : journey.path("sections").values()) {
            JourneySection mapped = toSection(section, lines);
            // Zero-length links between an address and a stop are noise
            if (mapped != null && !(mapped.kind() == Kind.WALK && mapped.duration() == 0)) {
                sections.add(mapped);
            }
        }

        JsonNode fare = journey.path("fare");
        Integer fareCents = fare.path("found").asBoolean(false) && "centime".equals(fare.path("total").path("currency").asString(""))
            ? (int) Math.round(fare.path("total").path("value").asDouble(0))
            : null;

        return new JourneyOption(
            journey.path("type").asString(null),
            journey.path("tags").values().stream().map(tag -> tag.asString("")).filter(StringUtils::isNotEmpty).toList(),
            parseTime(journey.path("departure_date_time").asString(null)),
            parseTime(journey.path("arrival_date_time").asString(null)),
            journey.path("duration").asInt(0),
            journey.path("nb_transfers").asInt(0),
            optionalInt(journey.path("durations").path("walking")),
            optionalInt(journey.path("distances").path("walking")),
            journey.path("co2_emission").path("value").isNumber() ? journey.path("co2_emission").path("value").asDouble() : null,
            fareCents,
            sections);
    }

    private JourneySection toSection(JsonNode section, Map<String, LineSummary> lines) {
        String type = section.path("type").asString("");
        String mode = section.path("mode").asString("");
        Kind kind = switch (type) {
            case "public_transport", "on_demand_transport" -> Kind.TRANSIT;
            case "transfer" -> Kind.TRANSFER;
            case "waiting" -> Kind.WAIT;
            case "street_network", "crow_fly" -> switch (mode) {
                case "bike", "bss" -> Kind.BIKE;
                case "car", "ridesharing", "taxi" -> Kind.CAR;
                default -> Kind.WALK;
            };
            case "bss_rent", "bss_put_back", "park", "leave_parking", "alighting", "boarding" -> null;
            default -> Kind.OTHER;
        };
        if (kind == null) {
            return null;
        }

        Instant departure = parseTime(section.path("departure_date_time").asString(null));
        Instant arrival = parseTime(section.path("arrival_date_time").asString(null));

        LineSummary line = null;
        String headsign = null;
        List<String> boardingPositions = List.of();
        List<JourneyStop> stops = List.of();
        Boolean realtime = null;
        Integer delay = null;

        if (kind == Kind.TRANSIT) {
            JsonNode display = section.path("display_informations");
            line = lineOf(section, display, lines);
            headsign = removeTown(display.path("direction").asString(display.path("headsign").asString(null)));
            boardingPositions = section.path("best_boarding_positions").values().stream()
                .map(position -> position.asString("").toLowerCase(Locale.ROOT))
                .filter(StringUtils::isNotEmpty)
                .toList();
            stops = section.path("stop_date_times").values().stream()
                .map(stop -> {
                    JsonNode coord = stop.path("stop_point").path("coord");
                    Instant time = parseTime(stop.path("departure_date_time").asString(stop.path("arrival_date_time").asString(null)));
                    return new JourneyStop(stop.path("stop_point").path("name").asString(""),
                        coord.path("lat").asDouble(0), coord.path("lon").asDouble(0), time);
                })
                .toList();
            realtime = "realtime".equals(section.path("data_freshness").asString(""));
            Instant base = parseTime(section.path("base_departure_date_time").asString(null));
            if (realtime && base != null && departure != null) {
                delay = (int) (departure.getEpochSecond() - base.getEpochSecond());
            }
        }

        List<WalkStep> steps = section.path("path").values().stream()
            .filter(step -> StringUtils.isNotBlank(step.path("instruction").asString(null)))
            .map(step -> new WalkStep(step.path("instruction").asString(""), step.path("length").asInt(0), step.path("duration").asInt(0)))
            .toList();
        Integer length = optionalInt(section.path("geojson").path("properties").path(0).path("length"));

        List<double[]> shape = new ArrayList<>();
        for (JsonNode point : section.path("geojson").path("coordinates").values()) {
            shape.add(new double[]{point.path(0).asDouble(0), point.path(1).asDouble(0)});
        }

        return new JourneySection(kind, departure, arrival, section.path("duration").asInt(0),
            toPoint(section.path("from")), toPoint(section.path("to")),
            line, headsign, boardingPositions, stops, steps, realtime, delay, length, shape);
    }

    /** GTFS line of the section (consistent badges with the rest of the app), else built from Navitia's display */
    private static LineSummary lineOf(JsonNode section, JsonNode display, Map<String, LineSummary> lines) {
        for (JsonNode link : section.path("links").values()) {
            if ("line".equals(link.path("type").asString(""))) {
                LineSummary line = lines.get(StringUtils.substringAfterLast(link.path("id").asString(""), ":"));
                if (line != null) {
                    return line;
                }
            }
        }

        TransportMode mode = switch (display.path("physical_mode").asString("")) {
            case "Métro" -> TransportMode.METRO;
            case "RER" -> TransportMode.RER;
            case "Tramway" -> TransportMode.TRAM;
            case "Train Transilien" -> TransportMode.TRANSILIEN;
            case "TER / Intercités" -> TransportMode.TER;
            default -> TransportMode.BUS;
        };
        return new LineSummary(null, display.path("code").asString(null), display.path("name").asString(null), mode,
            display.path("color").asString(null), display.path("text_color").asString(null));
    }

    private static JourneyPoint toPoint(JsonNode place) {
        if (place.isMissingNode() || place.isNull()) {
            return null;
        }
        String embeddedType = place.path("embedded_type").asString("");
        JsonNode object = place.path(embeddedType);
        JsonNode coord = object.path("coord");

        String stopAreaId = switch (embeddedType) {
            case "stop_area" -> object.path("id").asString(null);
            case "stop_point" -> object.path("stop_area").path("id").asString(null);
            default -> null;
        };
        return new JourneyPoint(
            place.path("name").asString(""),
            coord.path("lat").asDouble(0),
            coord.path("lon").asDouble(0),
            stopAreaId == null ? null : StringUtils.removeStart(stopAreaId, "stop_area:"));
    }

    /** {@code 48.85,2.35} → {@code 2.35;48.85}, {@code IDFM:71264} → {@code stop_area:IDFM:71264} */
    static String toNavitiaPlace(String place) {
        if (place == null || place.isBlank()) {
            throw new CustomException("Missing journey start or end", HttpStatus.BAD_REQUEST);
        }
        String value = place.trim();
        if (COORDINATES.matcher(value).matches()) {
            String[] parts = value.split(",");
            return parts[1] + ";" + parts[0];
        }
        if (STOP_AREA.matcher(value).matches()) {
            return "stop_area:" + value;
        }
        throw new CustomException("Invalid place: " + value + " (lat,lon or stop area id expected)", HttpStatus.BAD_REQUEST);
    }

    private static List<String> forbiddenModes(Collection<TransportMode> allowed) {
        if (allowed == null || allowed.isEmpty()) {
            return List.of();
        }
        Set<TransportMode> forbidden = EnumSet.allOf(TransportMode.class);
        forbidden.removeAll(allowed);

        // A physical mode is forbidden only if no allowed mode uses it (buses carry both BUS and NOCTILIEN)
        Set<String> allowedPhysical = new HashSet<>();
        allowed.forEach(mode -> allowedPhysical.addAll(PHYSICAL_MODES.get(mode)));
        return forbidden.stream()
            .flatMap(mode -> PHYSICAL_MODES.get(mode).stream())
            .filter(physical -> !allowedPhysical.contains(physical))
            .distinct()
            .toList();
    }

    /** Navitia "next"/"prev" links carry the datetime to request */
    private static PageCursor toCursor(String href) {
        if (href == null) {
            return null;
        }
        Map<String, List<String>> params = UriComponentsBuilder.fromUriString(href).build().getQueryParams();
        Instant datetime = parseTime(first(params.get("datetime")));
        return datetime == null ? null : new PageCursor(datetime, "arrival".equals(first(params.get("datetime_represents"))));
    }

    private static String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    /** Navitia local Paris time {@code 20261003T123116} */
    private static Instant parseTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, NAVITIA_DATETIME).atZone(PARIS).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** {@code Saint-Denis - Pleyel (Saint-Denis)} → {@code Saint-Denis - Pleyel} */
    private static String removeTown(String direction) {
        return direction == null ? null : direction.replaceFirst("\\s*\\([^()]*\\)$", "");
    }

    private static Integer optionalInt(JsonNode node) {
        return node.isNumber() ? node.asInt() : null;
    }
}
