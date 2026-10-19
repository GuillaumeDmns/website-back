package com.gdamiens.website.service;

import com.gdamiens.website.client.DisruptionsClient;
import com.gdamiens.website.client.ElevatorClient;
import com.gdamiens.website.controller.object.v2.Disruption;
import com.gdamiens.website.controller.object.v2.Disruption.Category;
import com.gdamiens.website.controller.object.v2.Disruption.Section;
import com.gdamiens.website.controller.object.v2.Disruption.Severity;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.LineTraffic;
import com.gdamiens.website.controller.object.v2.StopAreaDetail;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.utils.ParisTime;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Traffic info from PRIM {@code disruptions_bulk} ("Messages Info Trafic, requête globale", its own quota): one call
 * returns every disruption with the lines and stop points it impacts. Elevator outages come from the open data "État
 * des ascenseurs" (not PRIM). Each snapshot is shared by every caller and refreshed in the background when a request
 * finds it older than its max age (nothing is fetched while nobody asks): callers get the previous one meanwhile, and
 * keep it when the source fails.
 */
@Service
public class TrafficService {

    private static final Logger log = LoggerFactory.getLogger(TrafficService.class);

    /** At most 480 calls a day, while the app is used: IDFM's messages change a few times an hour */
    private static final Duration TRAFFIC_MAX_AGE = Duration.ofMinutes(3);

    /** The elevator data change a few times a day */
    private static final Duration ELEVATORS_MAX_AGE = Duration.ofHours(1);

    /** Without elevator data (source down at startup), tried again after this long */
    private static final Duration ELEVATORS_RETRY = Duration.ofMinutes(5);

    /** Upcoming disruptions shown this long before they start */
    private static final Duration UPCOMING = Duration.ofDays(7);

    /** Lines always listed in the traffic overview, even without disruption */
    private static final Set<TransportMode> MAIN_MODES = EnumSet.of(TransportMode.METRO, TransportMode.RER, TransportMode.TRANSILIEN, TransportMode.TRAM);

    private static final Pattern LINE_BREAK = Pattern.compile("(?i)<br\\b[^>]*>");

    private static final Pattern PARAGRAPH_END = Pattern.compile("(?i)</p>|</div>|</ul>");

    /** Starts a line of its own: the end of the previous item adds nothing */
    private static final Pattern LIST_ITEM = Pattern.compile("(?i)<li\\b[^>]*>");

    private static final Pattern TAG = Pattern.compile("<[^>]+>");

    private static final Pattern ENTITY = Pattern.compile("&(#?[A-Za-z0-9]+);");

    private static final Comparator<Disruption> DISRUPTION_ORDER = Comparator
        .comparing(Disruption::active).reversed()
        .thenComparing(Disruption::severity, Comparator.reverseOrder())
        .thenComparing(Disruption::start, Comparator.nullsLast(Comparator.naturalOrder()));

    private final NetworkService networkService;

    private final Refreshed<Snapshot> traffic;

    private final Refreshed<Map<String, List<Disruption>>> elevators;

    public TrafficService(DisruptionsClient disruptionsClient, ElevatorClient elevatorClient, NetworkService networkService,
                          NetworkRepository networkRepository) {
        this.networkService = networkService;
        StopLookup stops = new StopLookup() {
            @Override
            public Map<String, String> stopAreasOfQuays(Collection<String> quayIds) {
                return networkRepository.findStopAreasOfQuays(quayIds);
            }

            @Override
            public Map<String, String> names(Collection<String> stopAreaIds) {
                Map<String, String> names = new HashMap<>();
                networkRepository.findStopAreas(stopAreaIds).forEach((id, row) -> names.put(id, row.name()));
                return names;
            }
        };
        this.traffic = new Refreshed<>("traffic", TRAFFIC_MAX_AGE, () -> {
            Snapshot loaded = parse(disruptionsClient.getDisruptions(), stops, Instant.now());
            log.info("Loaded {} disruptions on {} lines and {} stop areas", loaded.disruptionCount(), loaded.lineDisruptions().size(),
                loaded.stopDisruptions().size());
            return loaded;
        }, null);
        this.elevators = new Refreshed<>("elevators", ELEVATORS_MAX_AGE, () -> {
            Map<String, List<Disruption>> loaded = parseElevators(elevatorClient.getElevators());
            log.info("{} elevators out of order in {} stop areas", loaded.values().stream().mapToInt(List::size).sum(), loaded.size());
            return loaded;
        }, Map.of());
    }

    /**
     * @return lines of the main modes (metro, RER, Transilien, tram) and every other line with an active disruption,
     *         in the usual line order
     */
    public List<LineTraffic> getTraffic() {
        Snapshot current = traffic.get();
        Map<String, LineSummary> lines = networkService.getLines();
        return lines.values().stream()
            .filter(line -> MAIN_MODES.contains(line.mode()) || current.lineDisruptions().containsKey(line.id()))
            .map(line -> {
                List<Disruption> active = current.forLine(line.id()).stream()
                    .filter(Disruption::active)
                    .toList();
                Severity severity = active.stream().map(Disruption::severity).max(Comparator.naturalOrder()).orElse(null);
                return new LineTraffic(line, severity, active.stream().map(Disruption::title).filter(Objects::nonNull).distinct().toList());
            })
            .filter(traffic -> MAIN_MODES.contains(traffic.line().mode()) || traffic.severity() != null)
            .sorted(Comparator.comparing(LineTraffic::line, NetworkService.LINE_ORDER))
            .toList();
    }

    /**
     * @return active and upcoming disruptions of a line, active and worst first
     */
    public List<Disruption> getLineDisruptions(String lineId) {
        Snapshot current = traffic.get();
        // Its stops not served too ("Métro 1 : arrêts non desservis" only impacts stop points)
        return distinct(Stream.concat(current.forLine(lineId).stream(), current.forLineStops(lineId).stream()).distinct()
            .sorted(DISRUPTION_ORDER).toList());
    }

    /**
     * Stop areas the line doesn't serve now: those of its stop points under an active blocking disruption ("Station
     * fermée", "Arrêts non desservis"). Empty when the traffic can't be fetched: it only completes the real time.
     */
    public Set<String> getClosedStops(String lineId) {
        try {
            return traffic.get().closedStops().getOrDefault(lineId, Set.of());
        } catch (RuntimeException e) {
            log.info("No closed stops for line {}: {}", lineId, e.getMessage());
            return Set.of();
        }
    }

    /** Lines that don't stop at the stop area now (see {@link #getClosedStops}) */
    public Set<String> getClosedLines(String stopAreaId) {
        try {
            return traffic.get().closedStops().entrySet().stream()
                .filter(entry -> entry.getValue().contains(stopAreaId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        } catch (RuntimeException e) {
            log.info("No closed lines at {}: {}", stopAreaId, e.getMessage());
            return Set.of();
        }
    }

    /**
     * @return disruptions of the stop area itself (elevators, stop not served...) and active disruptions of its main
     *         lines (metro, RER, Transilien, tram). Bus line disruptions are left out: most of them are about other stops.
     */
    public List<Disruption> getStopDisruptions(StopAreaDetail stopArea) {
        Snapshot current = traffic.get();
        Set<Disruption> disruptions = new LinkedHashSet<>(current.forStopArea(stopArea.id()));
        disruptions.addAll(elevators.get().getOrDefault(stopArea.id(), List.of()));
        stopArea.lines().stream()
            .filter(line -> MAIN_MODES.contains(line.mode()))
            .forEach(line -> current.forLine(line.id()).stream().filter(Disruption::active).forEach(disruptions::add));
        return distinct(disruptions.stream().sorted(DISRUPTION_ORDER).toList());
    }

    /** The same text often comes several times (one disruption per impact) */
    private static List<Disruption> distinct(List<Disruption> disruptions) {
        Set<List<Object>> seen = new HashSet<>();
        return disruptions.stream()
            .filter(disruption -> seen.add(Arrays.asList(disruption.title(), disruption.message(), disruption.active())))
            .toList();
    }

    /** When the traffic snapshot was fetched, empty before the first request */
    public Optional<Instant> snapshotTime() {
        return traffic.loadedAt();
    }

    /** When the elevator status was fetched, empty before the first request for a stop */
    public Optional<Instant> elevatorsSnapshotTime() {
        return elevators.loadedAt();
    }

    /**
     * Fetches the traffic now (one {@code disruptions_bulk} call), for an administrator
     *
     * @return when it was fetched
     */
    public Instant reloadTraffic() {
        return traffic.reload();
    }

    /** Fetches the elevator status now, for an administrator */
    public Instant reloadElevators() {
        return elevators.reload();
    }

    /** GTFS lookups of the parsing */
    interface StopLookup {

        /** Quay ({@code IDFM:22219}, {@code IDFM:monomodalStopPlace:58792}) → stop area */
        Map<String, String> stopAreasOfQuays(Collection<String> quayIds);

        /** Stop area → GTFS name */
        Map<String, String> names(Collection<String> stopAreaIds);
    }

    /** Disruptions of a {@code disruptions_bulk} answer, per line and per stop area */
    static Snapshot parse(JsonNode response, StopLookup stops, Instant now) {
        // Lines (impacts on a line or its network) and quays (impacts on stop points, with the line they are listed under)
        // of each disruption
        Map<String, Set<String>> linesByDisruption = new HashMap<>();
        Map<String, Set<String>> quaysByDisruption = new HashMap<>();
        Map<String, Map<String, Set<String>>> lineQuaysByDisruption = new HashMap<>();
        for (JsonNode line : response.path("lines").values()) {
            String lineId = lineId(line.path("id").asString(""));
            for (JsonNode impacted : line.path("impactedObjects").values()) {
                String type = impacted.path("type").asString("");
                for (JsonNode disruptionId : impacted.path("disruptionIds").values()) {
                    if ("stop_point".equals(type)) {
                        String quayId = StringUtils.removeStart(impacted.path("id").asString(""), "stop_point:");
                        quaysByDisruption.computeIfAbsent(disruptionId.asString(""), k -> new LinkedHashSet<>()).add(quayId);
                        lineQuaysByDisruption.computeIfAbsent(disruptionId.asString(""), k -> new HashMap<>())
                            .computeIfAbsent(lineId, k -> new LinkedHashSet<>()).add(quayId);
                    } else {
                        linesByDisruption.computeIfAbsent(disruptionId.asString(""), k -> new LinkedHashSet<>()).add(lineId);
                    }
                }
            }
        }

        Map<String, String> quayStopAreas = stops.stopAreasOfQuays(quaysByDisruption.values().stream().flatMap(Set::stream)
            .collect(Collectors.toSet()));
        Set<String> sectionStops = new HashSet<>();
        response.path("disruptions").values().forEach(node -> node.path("impactedSections").values().forEach(section -> {
            sectionStops.add(stopAreaId(section.path("from").path("id").asString("")));
            sectionStops.add(stopAreaId(section.path("to").path("id").asString("")));
        }));
        Map<String, String> names = sectionStops.isEmpty() ? Map.of() : stops.names(sectionStops);

        Map<String, List<Disruption>> lineDisruptions = new HashMap<>();
        Map<String, List<Disruption>> stopDisruptions = new HashMap<>();
        Map<String, List<Disruption>> lineStopDisruptions = new HashMap<>();
        Map<String, Set<String>> closedStops = new HashMap<>();
        int count = 0;
        for (JsonNode node : response.path("disruptions").values()) {
            String id = node.path("id").asString("");
            Disruption disruption = toDisruption(node, List.copyOf(linesByDisruption.getOrDefault(id, Set.of())), names, now);
            if (disruption == null) {
                continue;
            }
            count++;
            disruption.lineIds().forEach(lineId -> lineDisruptions.computeIfAbsent(lineId, k -> new ArrayList<>()).add(disruption));
            quaysByDisruption.getOrDefault(id, Set.of()).stream()
                .map(quayStopAreas::get)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(stopAreaId -> stopDisruptions.computeIfAbsent(stopAreaId, k -> new ArrayList<>()).add(disruption));
            lineQuaysByDisruption.getOrDefault(id, Map.of()).forEach((lineId, quayIds) -> {
                if (!disruption.lineIds().contains(lineId)) {
                    lineStopDisruptions.computeIfAbsent(lineId, k -> new ArrayList<>()).add(disruption);
                }
                if (disruption.active() && disruption.severity() == Severity.BLOCKING) {
                    quayIds.stream().map(quayStopAreas::get).filter(Objects::nonNull)
                        .forEach(stopAreaId -> closedStops.computeIfAbsent(lineId, k -> new HashSet<>()).add(stopAreaId));
                }
            });
        }
        return new Snapshot(lineDisruptions, stopDisruptions, lineStopDisruptions, closedStops, count);
    }

    /** {@code line:IDFM:C01371} → {@code C01371} */
    private static String lineId(String id) {
        return StringUtils.substringAfterLast(id, ":");
    }

    /** {@code stop_area:IDFM:71264} → {@code IDFM:71264} */
    private static String stopAreaId(String id) {
        return StringUtils.removeStart(id, "stop_area:");
    }

    /** null for past disruptions and upcoming ones starting later than {@link #UPCOMING} */
    private static Disruption toDisruption(JsonNode node, List<String> lineIds, Map<String, String> names, Instant now) {
        // Current period, else the next one (periods are not always in order)
        Instant start = null;
        Instant end = null;
        boolean active = false;
        for (JsonNode period : node.path("applicationPeriods").values()) {
            Instant periodStart = ParisTime.parseNavitia(period.path("begin").asString(null));
            Instant periodEnd = ParisTime.parseNavitia(period.path("end").asString(null));
            if (periodEnd != null && !periodEnd.isAfter(now)) {
                continue;
            }
            if (periodStart == null || !periodStart.isAfter(now)) {
                active = true;
                start = periodStart;
                end = periodEnd;
                break;
            }
            if (start == null || periodStart.isBefore(start)) {
                start = periodStart;
                end = periodEnd;
            }
        }
        if (!active && (start == null || start.isAfter(now.plus(UPCOMING)))) {
            return null;
        }

        String cause = StringUtils.trimToNull(node.path("cause").asString(null));
        Severity severity = switch (node.path("severity").asString("")) {
            case "BLOQUANTE" -> Severity.BLOCKING;
            case "PERTURBEE" -> Severity.DISRUPTED;
            default -> Severity.INFO;
        };
        // IDFM lists a section both ways: kept once
        Set<List<String>> seenSections = new HashSet<>();
        List<Section> sections = node.path("impactedSections").values().stream()
            .map(section -> {
                String fromId = stopAreaId(section.path("from").path("id").asString(""));
                String toId = stopAreaId(section.path("to").path("id").asString(""));
                return new Section(lineId(section.path("lineId").asString("")), fromId,
                    names.getOrDefault(fromId, section.path("from").path("name").asString(null)), toId,
                    names.getOrDefault(toId, section.path("to").path("name").asString(null)));
            })
            .filter(section -> seenSections.add(List.of(section.lineId(),
                StringUtils.compare(section.fromId(), section.toId()) <= 0 ? section.fromId() : section.toId(),
                StringUtils.compare(section.fromId(), section.toId()) <= 0 ? section.toId() : section.fromId())))
            .toList();
        Set<String> allLineIds = new LinkedHashSet<>(lineIds);
        sections.forEach(section -> allLineIds.add(section.lineId()));
        String title = StringUtils.trimToNull(node.path("title").asString(null));

        return new Disruption(node.path("id").asString(""), severity, "TRAVAUX".equals(cause) ? Category.WORKS : Category.TRAFFIC,
            title != null ? title : StringUtils.trimToNull(node.path("shortMessage").asString(null)),
            toPlainText(node.path("message").asString(null)), cause, start, end, active,
            ParisTime.parseNavitia(node.path("lastUpdate").asString(null)), List.copyOf(allLineIds), sections);
    }

    /** Elevators out of order, per stop area ({@code zdcid} 71426 → {@code IDFM:71426}) */
    static Map<String, List<Disruption>> parseElevators(JsonNode elevators) {
        Map<String, List<Disruption>> byStopArea = new LinkedHashMap<>();
        for (JsonNode elevator : elevators.values()) {
            String zone = StringUtils.trimToNull(elevator.path("zdcid").asString(null));
            if (zone == null || !"notavailable".equals(elevator.path("liftstatus").asString(""))) {
                continue;
            }
            String direction = clean(elevator.path("liftdirection").asString(null));
            Instant since = parseLocal(elevator.path("liftstateupdate").asString(null));
            Disruption disruption = new Disruption(elevator.path("liftid").asString(""), Severity.INFO, Category.ELEVATOR,
                direction == null ? "Ascenseur hors service" : "Ascenseur hors service : " + direction,
                clean(elevator.path("liftsituation").asString(null)), StringUtils.trimToNull(elevator.path("liftreason").asString(null)),
                since, null, true, since, List.of(), List.of());
            byStopArea.computeIfAbsent("IDFM:" + zone, k -> new ArrayList<>()).add(disruption);
        }
        return byStopArea;
    }

    private static String clean(String text) {
        return text == null ? null : StringUtils.trimToNull(text.replaceAll("\\s+", " "));
    }

    /** {@code 2026-10-09T06:11}, Paris time */
    private static Instant parseLocal(String value) {
        try {
            return value == null ? null : LocalDateTime.parse(value).atZone(ParisTime.ZONE).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** IDFM web messages are small HTML fragments */
    static String toPlainText(String html) {
        if (html == null) {
            return null;
        }
        String text = PARAGRAPH_END.matcher(html).replaceAll("\n\n");
        text = LINE_BREAK.matcher(text).replaceAll("\n");
        text = TAG.matcher(LIST_ITEM.matcher(text).replaceAll("\n• ")).replaceAll("");
        text = ENTITY.matcher(text).replaceAll(match -> Matcher.quoteReplacement(decodeEntity(match.group(1))));
        // Lines kept, blank lines (paragraphs) at most one in a row
        String lines = text.replace('\u00a0', ' ').lines()
            .map(line -> line.replaceAll("\\s{2,}", " ").strip())
            .collect(Collectors.joining("\n"));
        return StringUtils.trimToNull(lines.replaceAll("\n{3,}", "\n\n"));
    }

    private static String decodeEntity(String entity) {
        try {
            if (entity.startsWith("#x") || entity.startsWith("#X")) {
                return Character.toString(Integer.parseInt(entity.substring(2), 16));
            }
            if (entity.startsWith("#")) {
                return Character.toString(Integer.parseInt(entity.substring(1)));
            }
        } catch (IllegalArgumentException e) {
            return "&" + entity + ";";
        }
        return switch (entity) {
            case "nbsp" -> " ";
            case "amp" -> "&";
            case "lt" -> "<";
            case "gt" -> ">";
            case "quot" -> "\"";
            case "apos", "rsquo", "lsquo" -> "’";
            case "laquo" -> "«";
            case "raquo" -> "»";
            case "eacute" -> "é";
            case "egrave" -> "è";
            case "agrave" -> "à";
            case "ccedil" -> "ç";
            case "ecirc" -> "ê";
            case "euro" -> "€";
            default -> "&" + entity + ";";
        };
    }

    /**
     * @param lineStopDisruptions per line, the disruptions of its stop points that are not on the line itself
     * @param closedStops         per line, the stop areas it doesn't serve now (see {@link #getClosedStops})
     */
    record Snapshot(Map<String, List<Disruption>> lineDisruptions, Map<String, List<Disruption>> stopDisruptions,
                    Map<String, List<Disruption>> lineStopDisruptions, Map<String, Set<String>> closedStops, int disruptionCount) {

        List<Disruption> forLine(String lineId) {
            return lineDisruptions.getOrDefault(lineId, List.of());
        }

        List<Disruption> forLineStops(String lineId) {
            return lineStopDisruptions.getOrDefault(lineId, List.of());
        }

        List<Disruption> forStopArea(String stopAreaId) {
            return stopDisruptions.getOrDefault(stopAreaId, List.of());
        }
    }

    /**
     * A value loaded on the first request, then reloaded in the background by the first request that finds it older
     * than [maxAge]. A failed reload keeps the previous value. With a [fallback], a failed first load gives it for
     * {@link #ELEVATORS_RETRY} instead of failing the request.
     */
    private static final class Refreshed<T> {

        private record Loaded<T>(T value, Instant at, Instant expiresAt) {
        }

        private final String name;

        private final Duration maxAge;

        private final Supplier<T> loader;

        private final T fallback;

        private final AtomicBoolean refreshing = new AtomicBoolean();

        private volatile Loaded<T> loaded;

        Refreshed(String name, Duration maxAge, Supplier<T> loader, T fallback) {
            this.name = name;
            this.maxAge = maxAge;
            this.loader = loader;
            this.fallback = fallback;
        }

        T get() {
            Loaded<T> current = loaded;
            if (current == null) {
                synchronized (this) {
                    if (loaded == null) {
                        loaded = firstLoad();
                    }
                    return loaded.value();
                }
            }
            if (current.expiresAt().isBefore(Instant.now()) && refreshing.compareAndSet(false, true)) {
                Thread.ofVirtual().name(name + "-refresh").start(() -> {
                    try {
                        loaded = load();
                    } catch (RuntimeException e) {
                        log.warn("{} refresh failed, keeping the data of {}", name, current.at(), e);
                    } finally {
                        refreshing.set(false);
                    }
                });
            }
            return current.value();
        }

        /** Loads it now, keeping the previous value on failure */
        synchronized Instant reload() {
            Loaded<T> reloaded = load();
            loaded = reloaded;
            return reloaded.at();
        }

        Optional<Instant> loadedAt() {
            return Optional.ofNullable(loaded).map(Loaded::at);
        }

        private Loaded<T> firstLoad() {
            try {
                return load();
            } catch (RuntimeException e) {
                if (fallback == null) {
                    throw e;
                }
                log.warn("{} unavailable, tried again in {}", name, ELEVATORS_RETRY, e);
                Instant now = Instant.now();
                return new Loaded<>(fallback, now, now.plus(ELEVATORS_RETRY));
            }
        }

        private Loaded<T> load() {
            T value = loader.get();
            Instant now = Instant.now();
            return new Loaded<>(value, now, now.plus(maxAge));
        }
    }
}
