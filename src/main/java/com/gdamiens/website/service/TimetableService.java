package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.controller.object.v2.Timetable;
import com.gdamiens.website.controller.object.v2.TimetableDirection;
import com.gdamiens.website.controller.object.v2.TimetableEntry;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.TimetableRow;
import com.gdamiens.website.utils.TtlCache;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Scheduled timetable of a line at a stop area for a day, from the GTFS. Cached 6 h (the GTFS changes twice a day).
 */
@Service
public class TimetableService {

    /** Destinations named in a direction's name */
    private static final int NAMED_DESTINATIONS = 3;

    private final NetworkService networkService;

    private final NetworkRepository networkRepository;

    private final TtlCache<Key, Optional<Timetable>> cache = new TtlCache<>(Duration.ofHours(6), 2000);

    /** A new GTFS replaced the previous one */
    @EventListener(GtfsImportedEvent.class)
    public void onGtfsImported() {
        cache.invalidateAll();
    }

    public TimetableService(NetworkService networkService, NetworkRepository networkRepository) {
        this.networkService = networkService;
        this.networkRepository = networkRepository;
    }

    private record Key(String stopAreaId, String lineId, LocalDate date) {
    }

    /**
     * @return empty when the stop area or the line is unknown
     */
    public Optional<Timetable> getTimetable(String stopAreaId, String lineId, LocalDate date) {
        return cache.get(new Key(stopAreaId, lineId, date), this::load);
    }

    private Optional<Timetable> load(Key key) {
        Optional<StopAreaSummary> stop = networkService.getStopAreaSummary(key.stopAreaId());
        Optional<LineSummary> line = networkService.getLine(key.lineId());
        if (stop.isEmpty() || line.isEmpty()) {
            return Optional.empty();
        }

        // Per direction of the line (where the trip's last stop is after this one; GTFS direction ids are unreliable),
        // else per GTFS direction. Trips ending here are arrivals, not departures.
        Map<String, Integer> directionOf = directionsOfTermini(key);
        Map<Integer, List<TimetableRow>> byDirection = new TreeMap<>();
        // The GTFS sometimes lists a train under several calendars active the same day: once per time and destination
        // (two trains may still leave the same minute, e.g. a direct one and a stopping one)
        Set<String> seen = new HashSet<>();
        for (TimetableRow row : networkRepository.findTimetable(key.stopAreaId(), IDFMRoute.toRouteId(key.lineId()), key.date())) {
            if (!key.stopAreaId().equals(row.terminusId()) && seen.add(row.departureSeconds() + "|" + row.terminusId())) {
                int direction = Optional.ofNullable(directionOf.get(row.terminusId()))
                    .orElse(100 + Optional.ofNullable(row.directionId()).map(Short::intValue).orElse(0));
                byDirection.computeIfAbsent(direction, d -> new ArrayList<>()).add(row);
            }
        }

        List<TimetableDirection> directions = byDirection.values().stream()
            .map(rows -> new TimetableDirection(name(rows), rows.stream()
                .map(row -> new TimetableEntry(DepartureService.toInstant(key.date(), row.departureSeconds()), row.terminus(),
                    DepartureService.isMission(row.headsign()) ? row.headsign() : null))
                .toList()))
            .toList();
        return Optional.of(new Timetable(stop.get(), line.get(), key.date(), directions));
    }

    /** Index of the line's direction serving each stop after this one */
    private Map<String, Integer> directionsOfTermini(Key key) {
        Map<String, Integer> directions = new java.util.HashMap<>();
        networkService.getLineDetail(key.lineId()).ifPresent(detail -> {
            for (int d = 0; d < detail.directions().size(); d++) {
                for (var branch : detail.directions().get(d).branches()) {
                    List<String> ids = branch.stops().stream().map(stop -> stop.id()).toList();
                    int here = ids.indexOf(key.stopAreaId());
                    if (here >= 0) {
                        for (String id : ids.subList(here + 1, ids.size())) {
                            directions.putIfAbsent(id, d);
                        }
                    }
                }
            }
        });
        return directions;
    }

    /** Most served destinations first */
    private static String name(List<TimetableRow> rows) {
        Map<String, Long> counts = rows.stream()
            .collect(Collectors.groupingBy(TimetableRow::terminus, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(NAMED_DESTINATIONS)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" / "));
    }
}
