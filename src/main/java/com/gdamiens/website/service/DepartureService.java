package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.CallUnit;
import com.gdamiens.website.controller.object.v2.Departure;
import com.gdamiens.website.controller.object.v2.LineDepartures;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.controller.object.v2.StopDepartures;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.ScheduledDepartureRow;
import com.gdamiens.website.utils.Constants;
import com.gdamiens.website.utils.TtlCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Next departures of a stop area: PRIM stop-monitoring (real time) for the lines it covers, GTFS schedule for the
 * other lines (or all of them when PRIM cannot be reached). Each stop area is fetched from PRIM at most every 30 s
 * whatever the number of clients.
 */
@Service
public class DepartureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartureService.class);

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    /** Departures kept per line and destination */
    public static final int MAX_DEPARTURES = 5;

    private static final Duration SCHEDULE_WINDOW = Duration.ofHours(2);

    /** Departures that just left are still shown for a short while */
    private static final Duration PAST_MARGIN = Duration.ofSeconds(60);

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final IDFMStopGtfsService idfmStopGtfsService;

    private final TtlCache<String, Optional<StopDepartures>> cache = new TtlCache<>(Duration.ofSeconds(30), 10000);

    public DepartureService(NetworkService networkService, NetworkRepository networkRepository, IDFMStopGtfsService idfmStopGtfsService) {
        this.networkService = networkService;
        this.networkRepository = networkRepository;
        this.idfmStopGtfsService = idfmStopGtfsService;
    }

    /**
     * @param lineId only this line when not null
     * @param limit  departures per line and destination (at most {@link #MAX_DEPARTURES})
     */
    public Optional<StopDepartures> getDepartures(String stopAreaId, String lineId, int limit) {
        return cache.get(stopAreaId, this::loadDepartures)
            .map(departures -> filter(departures, lineId, limit));
    }

    /**
     * Departures of the stop areas around a position, closest first. Stop areas are fetched in parallel.
     */
    public List<StopDepartures> getNearbyDepartures(double lat, double lon, int radius, int maxStops, int limit) {
        List<StopAreaSummary> stops = networkService.getNearbyStopAreas(lat, lon, radius, maxStops);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Optional<StopDepartures>>> futures = stops.stream()
                .map(stop -> executor.submit(() -> getDepartures(stop.id(), null, limit)))
                .toList();

            List<StopDepartures> result = new ArrayList<>();
            for (int i = 0; i < stops.size(); i++) {
                StopAreaSummary stop = stops.get(i);
                getQuietly(futures.get(i))
                    .map(departures -> new StopDepartures(stop, departures.realtimeAvailable(), departures.lines()))
                    .ifPresent(result::add);
            }
            return result;
        }
    }

    private Optional<StopDepartures> loadDepartures(String stopAreaId) {
        return networkService.getStopAreaSummary(stopAreaId).map(stop -> {
            Instant now = Instant.now();
            Map<String, LineSummary> lines = networkService.getLines();

            List<CallUnit> calls = null;
            try {
                calls = idfmStopGtfsService.getStopNextPassage(stopAreaId, null, Constants.IDFM_STOP_MONITORING_URL);
            } catch (RuntimeException e) {
                LOGGER.warn("Real-time departures unavailable for {}: {}", stopAreaId, e.getMessage());
            }

            Map<GroupKey, List<Departure>> groups = new LinkedHashMap<>();

            if (calls != null) {
                for (CallUnit call : calls) {
                    LineSummary line = Optional.ofNullable(call.getLineId()).map(DepartureService::siriLineToLineId).map(lines::get).orElse(null);
                    String destination = firstNonBlank(call.getDestinationName(), call.getDestinationDisplay(), call.getDirectionName());
                    Departure departure = toDeparture(call);

                    if (line != null && destination != null && departure != null && !isTerminating(destination, stop)
                        && departure.time().isAfter(now.minus(PAST_MARGIN))) {
                        groups.computeIfAbsent(new GroupKey(line, destination), k -> new ArrayList<>()).add(departure);
                    }
                }
            }

            // Lines of the stop area without real-time data fall back to the schedule
            Set<String> realtimeLineIds = groups.keySet().stream().map(key -> key.line().id()).collect(Collectors.toSet());
            Set<String> scheduledLineIds = stop.lines().stream().map(LineSummary::id)
                .filter(id -> !realtimeLineIds.contains(id))
                .collect(Collectors.toSet());

            if (!scheduledLineIds.isEmpty()) {
                addScheduledDepartures(stop, now, scheduledLineIds, lines, groups);
            }

            List<LineDepartures> lineDepartures = groups.entrySet().stream()
                .map(e -> new LineDepartures(e.getKey().line(), e.getKey().destination(), e.getValue().stream()
                    .sorted(Comparator.comparing(Departure::time))
                    .limit(MAX_DEPARTURES)
                    .toList()))
                .sorted(Comparator.comparing(LineDepartures::line, NetworkService.LINE_ORDER)
                    .thenComparing(LineDepartures::destination))
                .toList();

            return new StopDepartures(stop, calls != null, lineDepartures);
        });
    }

    private void addScheduledDepartures(StopAreaSummary stop, Instant now, Set<String> lineIds, Map<String, LineSummary> lines,
                                        Map<GroupKey, List<Departure>> groups) {
        ZonedDateTime localNow = now.atZone(PARIS);
        int nowSeconds = localNow.toLocalTime().toSecondOfDay();

        List<ScheduledDepartureRow> rows = networkRepository.findScheduledDepartures(stop.id(), localNow.toLocalDate(),
            nowSeconds - (int) PAST_MARGIN.toSeconds(), nowSeconds + (int) SCHEDULE_WINDOW.toSeconds());

        for (ScheduledDepartureRow row : rows) {
            String lineId = IDFMRoute.toLineId(row.routeId());
            LineSummary line = lines.get(lineId);
            if (line == null || row.headsign() == null || !lineIds.contains(lineId) || isTerminating(row.headsign(), stop)) {
                continue;
            }

            Instant time = toInstant(row.serviceDate(), row.departureSeconds());
            groups.computeIfAbsent(new GroupKey(line, row.headsign()), k -> new ArrayList<>())
                .add(new Departure(time, time, false, null, null, null));
        }
    }

    private static StopDepartures filter(StopDepartures departures, String lineId, int limit) {
        int max = Math.clamp(limit, 1, MAX_DEPARTURES);
        return new StopDepartures(departures.stop(), departures.realtimeAvailable(), departures.lines().stream()
            .filter(line -> lineId == null || line.line().id().equals(lineId))
            .map(line -> new LineDepartures(line.line(), line.destination(), line.departures().stream().limit(max).toList()))
            .toList());
    }

    private static Departure toDeparture(CallUnit call) {
        Instant aimed = parse(firstNonBlank(call.getAimedDepartureTime(), call.getAimedArrivalTime()));
        Instant expected = parse(firstNonBlank(call.getExpectedDepartureTime(), call.getExpectedArrivalTime()));
        Instant time = expected != null ? expected : aimed;
        if (time == null) {
            return null;
        }

        return new Departure(time, aimed, true,
            firstNonBlank(call.getDepartureStatus(), call.getArrivalStatus()),
            StringUtils.trimToNull(call.getArrivalPlatformName()),
            call.getVehicleAtStop());
    }

    /**
     * Vehicles ending their trip at this stop are not departures. The destination may be longer than the stop name
     * ("Paris Gare de Lyon" at "Gare de Lyon"), so whole-word containment counts too.
     */
    static boolean isTerminating(String destination, StopAreaSummary stop) {
        String normalizedDestination = normalize(destination);
        String normalizedStop = normalize(stop.name());
        return normalizedDestination.equals(normalizedStop)
            || (" " + normalizedDestination + " ").contains(" " + normalizedStop + " ");
    }

    private static String normalize(String name) {
        return StringUtils.stripAccents(StringUtils.defaultString(name)).toLowerCase(java.util.Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** {@code STIF:Line::C01371:} → {@code C01371} */
    static String siriLineToLineId(String siriLineRef) {
        String[] parts = siriLineRef.split(":");
        return Stream.of(parts).filter(StringUtils::isNotBlank).reduce((first, second) -> second).orElse(null);
    }

    /** GTFS times are counted from noon minus 12 h of the service day, which matters on DST change days */
    private static Instant toInstant(LocalDate serviceDate, int seconds) {
        return ZonedDateTime.of(serviceDate, LocalTime.NOON, PARIS).minusHours(12).plusSeconds(seconds).toInstant();
    }

    private static Instant parse(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        return Stream.of(values).filter(StringUtils::isNotBlank).findFirst().orElse(null);
    }

    private static <T> Optional<T> getQuietly(Future<Optional<T>> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.warn("Nearby departures failed for one stop area", e);
            return Optional.empty();
        }
    }

    private record GroupKey(LineSummary line, String destination) {
        GroupKey {
            Objects.requireNonNull(line);
            Objects.requireNonNull(destination);
        }
    }
}
