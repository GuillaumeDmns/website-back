package com.gdamiens.website.service;

import com.gdamiens.website.client.NavitiaClient;
import com.gdamiens.website.controller.object.v2.BikeStation;
import com.gdamiens.website.controller.object.v2.JourneyOption;
import com.gdamiens.website.controller.object.v2.JourneyPlan;
import com.gdamiens.website.controller.object.v2.JourneyPlan.PageCursor;
import com.gdamiens.website.controller.object.v2.JourneyPoint;
import com.gdamiens.website.controller.object.v2.JourneySection;
import com.gdamiens.website.controller.object.v2.JourneySection.Kind;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.utils.Geo;
import com.gdamiens.website.utils.GeoJson;
import com.gdamiens.website.utils.ParisTime;
import com.gdamiens.website.utils.TtlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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

    private final NavitiaClient navitiaClient;

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final BikeService bikeService;

    /** Same search within a minute (see {@link #plan}) */
    private final TtlCache<CachedSearch, JourneyPlan> journeyCache = new TtlCache<>(Duration.ofSeconds(60), 5000);

    /** About 50 m north-south (and 37 m east-west at Paris' latitude) */
    private static final double GRID = 0.0005;

    private final TtlCache<String, RidePath> rideCache = new TtlCache<>(Duration.ofHours(24), 5000);

    /** With the caller's authentication: ApiQuota counts guests apart (the Vélib option calls Navitia) */
    private final ExecutorService shapeExecutor = new DelegatingSecurityContextExecutorService(Executors.newVirtualThreadPerTaskExecutor());

    private final TtlCache<String, Optional<List<double[]>>> shapeCache = new TtlCache<>(Duration.ofHours(6), 20000);

    /** A new GTFS replaced the previous one: its shapes have other ids */
    @EventListener(GtfsImportedEvent.class)
    public void onGtfsImported() {
        shapeCache.invalidateAll();
    }

    public JourneyService(NavitiaClient navitiaClient, NetworkService networkService, NetworkRepository networkRepository,
                          BikeService bikeService) {
        this.navitiaClient = navitiaClient;
        this.networkService = networkService;
        this.networkRepository = networkRepository;
        this.bikeService = bikeService;
    }

    /**
     * Journeys of [query]. The same search (positions within about 50 m, same minute, same options) within a minute is
     * answered from a cache, without calling Navitia: shared links, going back, GO recalculations of travellers side by
     * side.
     */
    public JourneyPlan plan(JourneyQuery query) {
        JourneyQuery shared = withSharedPlaces(query);
        Instant minute = Optional.ofNullable(query.datetime()).orElseGet(Instant::now).truncatedTo(ChronoUnit.MINUTES);
        JourneyPlan plan = journeyCache.get(new CachedSearch(shared, minute), search -> compute(search.query()));
        // GTFS paths have their own cache: one not ready for the first answer comes with the next ones
        return new JourneyPlan(withGtfsShapes(plan.journeys()), plan.earlier(), plan.later());
    }

    /** A search and the minute it is for (now when the query has no time) */
    private record CachedSearch(JourneyQuery query, Instant minute) {
    }

    /** Positions on a grid of about 50 m, so that close searches share their answer; times to the minute */
    static JourneyQuery withSharedPlaces(JourneyQuery query) {
        return new JourneyQuery(gridded(query.from()), gridded(query.to()),
            query.datetime() == null ? null : query.datetime().truncatedTo(ChronoUnit.MINUTES), query.arriveBy(),
            query.modes() == null || query.modes().isEmpty() ? Set.of() : EnumSet.copyOf(query.modes()), query.wheelchair(),
            query.walkingSpeed(), query.maxTransfers(), query.bikeShare());
    }

    private static String gridded(String place) {
        if (place == null || !COORDINATES.matcher(place.trim()).matches()) {
            return place == null ? null : place.trim();
        }
        String[] parts = place.trim().split(",");
        return String.format(Locale.ROOT, "%.4f,%.4f", grid(Double.parseDouble(parts[0])), grid(Double.parseDouble(parts[1])));
    }

    private static double grid(double degrees) {
        return Math.round(degrees / GRID) * GRID;
    }

    private JourneyPlan compute(JourneyQuery query) {
        // Navitia has no Vélib stations in Île-de-France: that option is built here, alongside
        CompletableFuture<Optional<JourneyOption>> bikeShare = query.bikeShare()
            ? CompletableFuture.supplyAsync(() -> bikeShareJourney(query), shapeExecutor)
            : CompletableFuture.completedFuture(Optional.empty());
        JsonNode response;
        try {
            response = navitiaClient.planJourneys(
                toNavitiaPlace(query.from()),
                toNavitiaPlace(query.to()),
                query.datetime() == null ? null : ParisTime.formatNavitia(query.datetime()),
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
            journeys.add(NavitiaJourneys.toJourney(journey, lines));
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
                case "prev" -> earlier = NavitiaJourneys.toCursor(link.path("href").asString(null));
                case "next" -> later = NavitiaJourneys.toCursor(link.path("href").asString(null));
                default -> {
                }
            }
        }
        return new JourneyPlan(journeys, earlier, later);
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
            JsonNode journey = navitiaClient.planWalkingPath(take.lon() + ";" + take.lat(), leave.lon() + ";" + leave.lat(), null, null)
                .path("journeys").path(0);
            List<JourneySection> sections = journey.isMissingNode() ? List.of() : NavitiaJourneys.toJourney(journey, Map.of()).sections();
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
        return (int) Math.round(Geo.meters(lat1, lon1, lat2, lon2) * 1.3);
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

    static List<String> forbiddenModes(Collection<TransportMode> allowed) {
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

}
