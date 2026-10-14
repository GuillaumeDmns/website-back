package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.DayPeriod;
import com.gdamiens.website.controller.object.v2.Frequency;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineServiceHours;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.ServiceDirection;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.controller.object.v2.StopServiceHours;
import com.gdamiens.website.controller.object.v2.Timetable;
import com.gdamiens.website.controller.object.v2.TimetableEntry;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.TripCallRow;
import com.gdamiens.website.utils.ParisTime;
import com.gdamiens.website.utils.TtlCache;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * First and last departures and frequency per period of the day, from the GTFS only (no real-time call): of each line
 * at a stop area, and of each direction of a line. Cached 6 h (the GTFS changes twice a day).
 */
@Service
public class ServiceHoursService {

    /** Destinations named in a direction's name */
    private static final int NAMED_DESTINATIONS = 3;

    /** Share of the gaps between departures left out at each end of the usual range (the odd longer or shorter one) */
    private static final double OUTLIERS = 0.2;

    private final NetworkService networkService;

    private final TimetableService timetableService;

    private final NetworkRepository networkRepository;

    private final TtlCache<Key, Optional<StopServiceHours>> stopCache = new TtlCache<>(Duration.ofHours(6), 2000);

    private final TtlCache<Key, Optional<LineServiceHours>> lineCache = new TtlCache<>(Duration.ofHours(6), 2000);

    public ServiceHoursService(NetworkService networkService, TimetableService timetableService, NetworkRepository networkRepository) {
        this.networkService = networkService;
        this.timetableService = timetableService;
        this.networkRepository = networkRepository;
    }

    /** A new GTFS replaced the previous one */
    @EventListener(GtfsImportedEvent.class)
    public void onGtfsImported() {
        stopCache.invalidateAll();
        lineCache.invalidateAll();
    }

    /** A stop area or a line, and a service day */
    private record Key(String id, LocalDate date) {
    }

    /**
     * Each line leaving the stop area, per direction (those of its timetable)
     *
     * @return empty when the stop area is unknown
     */
    public Optional<StopServiceHours> getStopServiceHours(String stopAreaId, LocalDate date) {
        return stopCache.get(new Key(stopAreaId, date), key -> networkService.getStopAreaSummary(key.id()).map(stop -> {
            List<LineServiceHours> lines = new ArrayList<>();
            for (LineSummary line : stop.lines()) {
                timetableService.getTimetable(key.id(), line.id(), key.date())
                    .map(timetable -> fromTimetable(timetable, key.date()))
                    .filter(hours -> !hours.directions().isEmpty())
                    .ifPresent(lines::add);
            }
            return new StopServiceHours(new StopAreaSummary(stop.id(), stop.name(), stop.lat(), stop.lon(), null, stop.lines()),
                key.date(), lines);
        }));
    }

    /**
     * Each direction of the line (those of its detail), counted at its stop served by the most trips
     *
     * @return empty when the line is unknown
     */
    public Optional<LineServiceHours> getLineServiceHours(String lineId, LocalDate date) {
        return lineCache.get(new Key(lineId, date), key -> networkService.getLineDetail(key.id())
            .map(detail -> new LineServiceHours(detail.line(), key.date(),
                directions(detail, networkRepository.findTripCalls(IDFMRoute.toRouteId(key.id()), key.date()), key.date()))));
    }

    private static LineServiceHours fromTimetable(Timetable timetable, LocalDate date) {
        List<ServiceDirection> directions = new ArrayList<>();
        timetable.directions().forEach(direction -> summarize(date, direction.departures().stream().map(TimetableEntry::time).toList())
            .ifPresent(summary -> directions.add(new ServiceDirection(direction.name(), null, null, summary.first(), summary.last(),
                summary.frequencies()))));
        return new LineServiceHours(timetable.line(), date, directions);
    }

    /** A trip of the day: its calls in order */
    private record Trip(List<TripCallRow> calls) {

        String origin() {
            return calls.getFirst().stopAreaId();
        }

        String terminus() {
            return calls.getLast().stopAreaId();
        }
    }

    /** Departures of each direction of [detail] at its busiest stop, from the trips' calls */
    static List<ServiceDirection> directions(LineDetail detail, List<TripCallRow> calls, LocalDate date) {
        Map<String, List<TripCallRow>> byTrip = new LinkedHashMap<>();
        calls.forEach(call -> byTrip.computeIfAbsent(call.tripId(), id -> new ArrayList<>()).add(call));

        // The GTFS sometimes lists a train under several calendars active the same day: once per stops and time
        Set<String> seen = new HashSet<>();
        Map<Integer, List<Trip>> tripsByDirection = new TreeMap<>();
        for (List<TripCallRow> tripCalls : byTrip.values()) {
            Trip trip = new Trip(tripCalls);
            String signature = tripCalls.stream().map(call -> call.stopAreaId() + "@" + call.departureSeconds()).collect(Collectors.joining(","));
            if (tripCalls.size() >= 2 && seen.add(signature)) {
                directionOf(detail, trip).ifPresent(direction -> tripsByDirection.computeIfAbsent(direction, d -> new ArrayList<>()).add(trip));
            }
        }

        Map<String, StopRef> stops = new HashMap<>();
        detail.directions().forEach(direction -> direction.branches().forEach(branch -> branch.stops().forEach(stop -> stops.putIfAbsent(stop.id(), stop))));

        List<ServiceDirection> directions = new ArrayList<>();
        tripsByDirection.forEach((index, trips) -> {
            List<String> order = detail.directions().get(index).branches().stream()
                .flatMap(branch -> branch.stops().stream().map(StopRef::id))
                .distinct()
                .toList();
            // Boarding trips per stop; the busiest one, the first of the direction among equals
            Map<String, Integer> boardings = new HashMap<>();
            trips.forEach(trip -> trip.calls().stream().filter(TripCallRow::pickup).map(TripCallRow::stopAreaId).distinct()
                .forEach(stop -> boardings.merge(stop, 1, Integer::sum)));
            Optional<String> busiest = boardings.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue()
                    .thenComparing(entry -> -Optional.of(order.indexOf(entry.getKey())).filter(i -> i >= 0).orElse(order.size())))
                .map(Map.Entry::getKey);
            if (busiest.isEmpty() || !stops.containsKey(busiest.get())) {
                return;
            }
            List<Instant> departures = trips.stream()
                .flatMap(trip -> trip.calls().stream().filter(call -> call.pickup() && call.stopAreaId().equals(busiest.get())).limit(1))
                .map(call -> ParisTime.ofServiceDay(date, call.departureSeconds()))
                .sorted()
                .toList();
            summarize(date, departures).ifPresent(summary -> directions.add(new ServiceDirection(name(trips, stops), index, stops.get(busiest.get()),
                summary.first(), summary.last(), summary.frequencies())));
        });
        return directions;
    }

    /** Direction whose branch serves the trip's first stop then its last one */
    private static Optional<Integer> directionOf(LineDetail detail, Trip trip) {
        for (int d = 0; d < detail.directions().size(); d++) {
            for (LineBranch branch : detail.directions().get(d).branches()) {
                List<String> ids = branch.stops().stream().map(StopRef::id).toList();
                int from = ids.indexOf(trip.origin());
                if (from >= 0 && ids.lastIndexOf(trip.terminus()) > from) {
                    return Optional.of(d);
                }
            }
        }
        return Optional.empty();
    }

    /** Most served destinations first */
    private static String name(List<Trip> trips, Map<String, StopRef> stops) {
        Map<String, Long> counts = trips.stream()
            .map(trip -> Optional.ofNullable(stops.get(trip.terminus())).map(StopRef::name).orElse(""))
            .filter(name -> !name.isEmpty())
            .collect(Collectors.groupingBy(name -> name, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(NAMED_DESTINATIONS)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" / "));
    }

    /** First and last departures of a service day, and the frequency per period */
    record Summary(Instant first, Instant last, List<Frequency> frequencies) {
    }

    /**
     * @param departures departures of the service day [date]
     * @return empty without departures
     */
    static Optional<Summary> summarize(LocalDate date, List<Instant> departures) {
        if (departures.isEmpty()) {
            return Optional.empty();
        }
        Instant midnight = ParisTime.ofServiceDay(date, 0);
        List<Instant> sorted = departures.stream().sorted().toList();
        TreeMap<DayPeriod, List<Integer>> indexes = new TreeMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            int seconds = (int) Duration.between(midnight, sorted.get(i)).toSeconds();
            indexes.computeIfAbsent(DayPeriod.of(seconds), period -> new ArrayList<>()).add(i);
        }

        List<Frequency> frequencies = new ArrayList<>();
        DayPeriod firstPeriod = indexes.firstKey();
        DayPeriod lastPeriod = indexes.lastKey();
        indexes.forEach((period, inPeriod) -> {
            // Gaps from each departure of the period to the next one (two departures the same minute, e.g. towards
            // two branches, are one for the traveller)
            List<Long> gaps = new ArrayList<>();
            for (int i : inPeriod) {
                if (i + 1 < sorted.size()) {
                    long gap = Duration.between(sorted.get(i), sorted.get(i + 1)).toSeconds();
                    if (gap >= 60) {
                        gaps.add(gap);
                    }
                }
            }
            gaps.sort(null);
            Integer min = gaps.isEmpty() ? null : minutes(gaps.get((int) Math.floor(OUTLIERS * (gaps.size() - 1))));
            Integer max = gaps.isEmpty() ? null : minutes(gaps.get((int) Math.ceil((1 - OUTLIERS) * (gaps.size() - 1))));
            Instant start = period == firstPeriod ? sorted.getFirst() : ParisTime.ofServiceDay(date, period.startSeconds());
            Instant end = period == lastPeriod ? sorted.getLast() : ParisTime.ofServiceDay(date, period.endSeconds());
            frequencies.add(new Frequency(period, start, end, inPeriod.size(), min, max));
        });
        return Optional.of(new Summary(sorted.getFirst(), sorted.getLast(), frequencies));
    }

    private static int minutes(long seconds) {
        return (int) Math.max(1, Math.round(seconds / 60.0));
    }
}
