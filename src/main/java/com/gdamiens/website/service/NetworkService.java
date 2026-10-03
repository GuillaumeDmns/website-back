package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.Connection;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineDirection;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.Quay;
import com.gdamiens.website.controller.object.v2.StopAreaDetail;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.QuayRow;
import com.gdamiens.website.repository.NetworkRepository.RouteRow;
import com.gdamiens.website.repository.NetworkRepository.StopAreaRow;
import com.gdamiens.website.repository.NetworkRepository.TripPatternRow;
import com.gdamiens.website.utils.TtlCache;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Network data (lines, stop areas, line branches) read from the GTFS. Results are cached: the GTFS changes at most
 * twice a day.
 */
@Service
public class NetworkService {

    private static final List<TransportMode> MODE_ORDER = List.of(
        TransportMode.METRO, TransportMode.RER, TransportMode.TRANSILIEN, TransportMode.TER,
        TransportMode.TRAM, TransportMode.BUS, TransportMode.NOCTILIEN);

    private static final Pattern LINE_NAME = Pattern.compile("^(\\D*)(\\d*)(.*)$");

    public static final Comparator<LineSummary> LINE_ORDER = Comparator
        .comparingInt((LineSummary line) -> MODE_ORDER.indexOf(line.mode()))
        .thenComparing(LineSummary::name, NetworkService::compareLineNames);

    private final NetworkRepository networkRepository;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final TtlCache<String, Map<String, LineSummary>> linesCache = new TtlCache<>(Duration.ofHours(1), 1);

    private final TtlCache<String, Optional<LineDetail>> lineDetailCache = new TtlCache<>(Duration.ofHours(6), 2000);

    private final TtlCache<String, Optional<StopAreaDetail>> stopAreaCache = new TtlCache<>(Duration.ofHours(1), 5000);

    public NetworkService(NetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
    }

    /**
     * @return every line, by IDFM line id ({@code C01371})
     */
    public Map<String, LineSummary> getLines() {
        return linesCache.get("all", k -> networkRepository.findAllRoutes()
            .stream()
            .map(NetworkService::toLineSummary)
            .collect(Collectors.toUnmodifiableMap(LineSummary::id, Function.identity())));
    }

    public Optional<LineSummary> getLine(String lineId) {
        return Optional.ofNullable(getLines().get(lineId));
    }

    public List<StopAreaSummary> getNearbyStopAreas(double lat, double lon, int radius, int limit) {
        List<StopAreaRow> rows = networkRepository.findNearbyStopAreas(lat, lon, radius, limit);
        Map<String, List<String>> routeIds = networkRepository.findRouteIdsByStopAreas(rows.stream().map(StopAreaRow::id).toList());

        return rows.stream()
            .map(row -> toSummary(row, routeIds.getOrDefault(row.id(), List.of())))
            .toList();
    }

    public Optional<StopAreaSummary> getStopAreaSummary(String stopAreaId) {
        return getStopArea(stopAreaId)
            .map(detail -> new StopAreaSummary(detail.id(), detail.name(), detail.lat(), detail.lon(), null, detail.lines()));
    }

    public Optional<StopAreaDetail> getStopArea(String stopAreaId) {
        return stopAreaCache.get(stopAreaId, id -> networkRepository.findStopArea(id).map(row -> {
            List<QuayRow> quayRows = networkRepository.findQuays(id);

            List<Quay> quays = quayRows.stream()
                .map(quay -> new Quay(quay.id(), quay.name(), quay.lat(), quay.lon(), quay.platformCode(),
                    quay.wheelchairBoarding(), quay.routeIds().stream().map(IDFMRoute::toLineId).sorted().toList()))
                .toList();

            List<Connection> connections = networkRepository.findConnections(id).stream()
                .map(connection -> new Connection(connection.id(), connection.name(), connection.minTransferSeconds()))
                .toList();

            List<String> routeIds = quayRows.stream().flatMap(quay -> quay.routeIds().stream()).distinct().toList();

            return new StopAreaDetail(row.id(), row.name(), row.lat(), row.lon(), aggregateWheelchairBoarding(quayRows),
                toLineSummaries(routeIds), quays, connections);
        }));
    }

    /**
     * Line with its stops per direction. Each direction lists its branches: trips are grouped by stop pattern and a
     * pattern included in a longer one (short turn, express service) is merged into it.
     */
    public Optional<LineDetail> getLineDetail(String lineId) {
        return lineDetailCache.get(lineId, id -> getLine(id).map(line -> {
            List<TripPatternRow> patterns = networkRepository.findTripPatterns(IDFMRoute.toRouteId(id));

            Map<Integer, List<TripPatternRow>> branchesByDirection = orientPatterns(patterns).stream()
                .collect(Collectors.groupingBy(TripPatternRow::directionId))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> mergeIncludedPatterns(e.getValue())));

            Set<String> stopIds = new HashSet<>();
            Set<String> shapeIds = new HashSet<>();
            branchesByDirection.values().forEach(branches -> branches.forEach(branch -> {
                stopIds.addAll(branch.stopAreaIds());
                if (branch.shapeId() != null) {
                    shapeIds.add(branch.shapeId());
                }
            }));

            Map<String, StopAreaRow> stops = networkRepository.findStopAreas(stopIds);
            Map<String, String> shapes = networkRepository.findShapesAsGeoJson(shapeIds);

            List<LineDirection> directions = branchesByDirection.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new LineDirection(e.getKey(), e.getValue().stream()
                    .map(branch -> toBranch(branch, stops, shapes))
                    .toList()))
                .toList();

            return new LineDetail(line, directions);
        }));
    }

    public List<LineSummary> toLineSummaries(Collection<String> routeIds) {
        Map<String, LineSummary> lines = getLines();
        return routeIds.stream()
            .map(routeId -> lines.get(IDFMRoute.toLineId(routeId)))
            .filter(Objects::nonNull)
            .distinct()
            .sorted(LINE_ORDER)
            .toList();
    }

    private StopAreaSummary toSummary(StopAreaRow row, List<String> routeIds) {
        return new StopAreaSummary(row.id(), row.name(), row.lat(), row.lon(), row.distance(), toLineSummaries(routeIds));
    }

    private LineBranch toBranch(TripPatternRow pattern, Map<String, StopAreaRow> stops, Map<String, String> shapes) {
        List<StopRef> stopRefs = pattern.stopAreaIds().stream()
            .map(stops::get)
            .filter(Objects::nonNull)
            .map(stop -> new StopRef(stop.id(), stop.name(), stop.lat(), stop.lon()))
            .toList();

        String headsign = stopRefs.isEmpty() ? pattern.headsign() : stopRefs.getLast().name();
        List<double[]> shape = Optional.ofNullable(pattern.shapeId()).map(shapes::get).map(this::parseLineString).orElse(List.of());

        return new LineBranch(headsign, pattern.tripCount(), stopRefs, shape);
    }

    private List<double[]> parseLineString(String geoJson) {
        List<double[]> coordinates = new ArrayList<>();
        JsonNode node = jsonMapper.readTree(geoJson).get("coordinates");
        if (node != null) {
            node.forEach(point -> coordinates.add(new double[]{point.get(0).asDouble(), point.get(1).asDouble()}));
        }
        return coordinates;
    }

    /**
     * GTFS direction_id is not always consistent (both ways of the RER A are in direction 0): each pattern gets
     * direction 0 if it runs the same way as the most frequent long pattern of direction 0, 1 if it runs the
     * opposite way, judged on their shared stops. Patterns sharing less than 2 stops with it keep their direction_id.
     */
    static List<TripPatternRow> orientPatterns(List<TripPatternRow> patterns) {
        Optional<TripPatternRow> reference = patterns.stream()
            .filter(p -> p.directionId() == 0)
            .max(Comparator.comparingInt((TripPatternRow p) -> p.stopAreaIds().size()).thenComparingInt(TripPatternRow::tripCount));
        if (reference.isEmpty()) {
            return patterns;
        }

        Map<String, Integer> referenceIndex = new HashMap<>();
        List<String> referenceStops = reference.get().stopAreaIds();
        for (int i = 0; i < referenceStops.size(); i++) {
            referenceIndex.putIfAbsent(referenceStops.get(i), i);
        }

        return patterns.stream().map(pattern -> {
            List<Integer> positions = pattern.stopAreaIds().stream().map(referenceIndex::get).filter(Objects::nonNull).toList();
            if (positions.size() < 2 || positions.getFirst().equals(positions.getLast())) {
                return pattern;
            }
            int direction = positions.getFirst() < positions.getLast() ? 0 : 1;
            return new TripPatternRow(direction, pattern.stopAreaIds(), pattern.tripCount(), pattern.shapeId(), pattern.headsign());
        }).toList();
    }

    /**
     * Merges stop patterns that only differ by skipped stops (express services, short turns) and keeps the others as
     * branches, most frequent first. A pattern is merged into a kept one when the stops they share are in the same
     * order and each stop missing from the kept one lies between two shared stops: the kept pattern then gets the
     * missing stops and the trips. A pattern starting or ending on stops the kept one does not have is another branch.
     */
    static List<TripPatternRow> mergeIncludedPatterns(List<TripPatternRow> patterns) {
        List<TripPatternRow> sorted = patterns.stream()
            .sorted(Comparator.comparingInt((TripPatternRow p) -> p.stopAreaIds().size()).reversed()
                .thenComparing(Comparator.comparingInt(TripPatternRow::tripCount).reversed()))
            .toList();

        List<TripPatternRow> kept = new ArrayList<>();

        for (TripPatternRow pattern : sorted) {
            boolean merged = false;
            for (int i = 0; i < kept.size() && !merged; i++) {
                TripPatternRow branch = kept.get(i);
                List<String> stops = mergeStops(branch.stopAreaIds(), pattern.stopAreaIds());
                if (stops != null) {
                    kept.set(i, new TripPatternRow(branch.directionId(), stops, branch.tripCount() + pattern.tripCount(),
                        branch.shapeId(), branch.headsign()));
                    merged = true;
                }
            }

            if (!merged) {
                kept.add(pattern);
            }
        }

        kept.sort(Comparator.comparingInt(TripPatternRow::tripCount).reversed());
        return kept;
    }

    /**
     * @return the stops of {@code branch} with the stops of {@code pattern} it skips inserted, or null if
     * {@code pattern} is not a variant of {@code branch} (stops in another order, or starting/ending elsewhere)
     */
    private static List<String> mergeStops(List<String> branch, List<String> pattern) {
        Map<String, Integer> branchIndex = new HashMap<>();
        for (int i = 0; i < branch.size(); i++) {
            branchIndex.putIfAbsent(branch.get(i), i);
        }

        // Shared stops must keep the same order, the pattern must start and end on shared stops
        List<Integer> anchors = new ArrayList<>();
        int previous = -1;
        for (int j = 0; j < pattern.size(); j++) {
            Integer i = branchIndex.get(pattern.get(j));
            if (i != null) {
                if (i <= previous) {
                    return null;
                }
                previous = i;
                anchors.add(j);
            }
        }
        if (anchors.isEmpty() || anchors.getFirst() != 0 || anchors.getLast() != pattern.size() - 1) {
            return null;
        }

        // Insert the skipped stops of each gap after the branch's own stops of that gap
        List<String> merged = new ArrayList<>(branch.subList(0, branchIndex.get(pattern.getFirst()) + 1));
        for (int a = 1; a < anchors.size(); a++) {
            int from = anchors.get(a - 1);
            int to = anchors.get(a);
            int branchFrom = branchIndex.get(pattern.get(from));
            int branchTo = branchIndex.get(pattern.get(to));

            merged.addAll(branch.subList(branchFrom + 1, branchTo));
            merged.addAll(pattern.subList(from + 1, to));
            merged.add(branch.get(branchTo));
        }
        merged.addAll(branch.subList(branchIndex.get(pattern.getLast()) + 1, branch.size()));
        return merged;
    }

    private static short aggregateWheelchairBoarding(List<QuayRow> quays) {
        Set<Short> values = quays.stream().map(QuayRow::wheelchairBoarding).map(v -> v == null ? (short) 0 : v).collect(Collectors.toSet());
        return values.size() == 1 ? values.iterator().next() : 0;
    }

    private static LineSummary toLineSummary(RouteRow route) {
        return new LineSummary(
            IDFMRoute.toLineId(route.routeId()),
            route.shortName(),
            route.longName(),
            TransportMode.fromGtfs(route.type(), route.agencyName(), route.shortName()),
            route.color(),
            route.textColor());
    }

    /**
     * Natural order on line names: {@code 2 < 10}, {@code T3a < T3b < T4}, {@code N1 < N12}.
     */
    private static int compareLineNames(String a, String b) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }

        Matcher ma = LINE_NAME.matcher(a);
        Matcher mb = LINE_NAME.matcher(b);
        if (!ma.matches() || !mb.matches()) {
            return a.compareTo(b);
        }

        int prefix = ma.group(1).compareTo(mb.group(1));
        if (prefix != 0) {
            return prefix;
        }

        long na = ma.group(2).isEmpty() ? -1 : Long.parseLong(ma.group(2));
        long nb = mb.group(2).isEmpty() ? -1 : Long.parseLong(mb.group(2));
        if (na != nb) {
            return Long.compare(na, nb);
        }
        return ma.group(3).compareTo(mb.group(3));
    }
}
