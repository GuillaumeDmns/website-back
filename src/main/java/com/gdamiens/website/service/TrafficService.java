package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.Disruption;
import com.gdamiens.website.controller.object.v2.Disruption.Category;
import com.gdamiens.website.controller.object.v2.Disruption.Severity;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.LineTraffic;
import com.gdamiens.website.controller.object.v2.StopAreaDetail;
import com.gdamiens.website.model.TransportMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Traffic info from Navitia {@code traffic_reports}: one call returns every disruption with the lines and stop areas
 * it is linked to (about 2 s, 25 MB). The snapshot is refreshed in the background when older than {@link #MAX_AGE}:
 * callers get the previous one meanwhile, and keep it if Navitia fails.
 */
@Service
public class TrafficService {

    private static final Logger log = LoggerFactory.getLogger(TrafficService.class);

    private static final Duration MAX_AGE = Duration.ofMinutes(2);

    /** Upcoming disruptions shown this long before they start */
    private static final Duration UPCOMING = Duration.ofDays(7);

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private static final DateTimeFormatter NAVITIA_DATETIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /** Lines always listed in the traffic overview, even without disruption */
    private static final Set<TransportMode> MAIN_MODES = EnumSet.of(TransportMode.METRO, TransportMode.RER, TransportMode.TRANSILIEN, TransportMode.TRAM);

    private static final Pattern PARAGRAPH_END = Pattern.compile("(?i)<br\\s*/?>|</p>|</li>|</div>");

    private static final Pattern TAG = Pattern.compile("<[^>]+>");

    private static final Pattern ENTITY = Pattern.compile("&(#?[A-Za-z0-9]+);");

    private static final Comparator<Disruption> DISRUPTION_ORDER = Comparator
        .comparing(Disruption::active).reversed()
        .thenComparing(Disruption::severity, Comparator.reverseOrder())
        .thenComparing(Disruption::start, Comparator.nullsLast(Comparator.naturalOrder()));

    private final IDFMNavitiaService navitiaService;

    private final NetworkService networkService;

    private final Object loadLock = new Object();

    private final AtomicBoolean refreshing = new AtomicBoolean();

    private volatile Snapshot snapshot;

    public TrafficService(IDFMNavitiaService navitiaService, NetworkService networkService) {
        this.navitiaService = navitiaService;
        this.networkService = networkService;
    }

    /**
     * @return lines of the main modes (metro, RER, Transilien, tram) and every other line with an active disruption,
     *         in the usual line order
     */
    public List<LineTraffic> getTraffic() {
        Snapshot current = current();
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
        return distinct(current().forLine(lineId).stream().sorted(DISRUPTION_ORDER).toList());
    }

    /**
     * @return disruptions of the stop area itself (elevators, stop not served...) and active disruptions of its main
     *         lines (metro, RER, Transilien, tram). Bus line disruptions are left out: most of them are about other stops.
     */
    public List<Disruption> getStopDisruptions(StopAreaDetail stopArea) {
        Snapshot current = current();
        Set<Disruption> disruptions = new LinkedHashSet<>(current.forStopArea(stopArea.id()));
        stopArea.lines().stream()
            .filter(line -> MAIN_MODES.contains(line.mode()))
            .forEach(line -> current.forLine(line.id()).stream().filter(Disruption::active).forEach(disruptions::add));
        return distinct(disruptions.stream().sorted(DISRUPTION_ORDER).toList());
    }

    /** Navitia has one disruption per impact: the same text often comes several times */
    private static List<Disruption> distinct(List<Disruption> disruptions) {
        Set<List<Object>> seen = new HashSet<>();
        return disruptions.stream()
            .filter(disruption -> seen.add(Arrays.asList(disruption.title(), disruption.message(), disruption.active())))
            .toList();
    }

    private Snapshot current() {
        Snapshot current = snapshot;
        if (current == null) {
            synchronized (loadLock) {
                if (snapshot == null) {
                    snapshot = load();
                }
                return snapshot;
            }
        }
        if (current.loadedAt().plus(MAX_AGE).isBefore(Instant.now()) && refreshing.compareAndSet(false, true)) {
            Thread.ofVirtual().name("traffic-refresh").start(() -> {
                try {
                    snapshot = load();
                } catch (RuntimeException e) {
                    log.warn("Traffic refresh failed, keeping the snapshot of {}", current.loadedAt(), e);
                } finally {
                    refreshing.set(false);
                }
            });
        }
        return current;
    }

    private Snapshot load() {
        long startedAt = System.nanoTime();
        JsonNode response = navitiaService.getAllTrafficReports();
        Instant now = Instant.now();

        // Lines and stop areas linked to each disruption
        Map<String, Set<String>> linesByDisruption = new HashMap<>();
        Map<String, List<String>> disruptionsByStopArea = new HashMap<>();
        for (JsonNode report : response.path("traffic_reports").values()) {
            for (JsonNode line : report.path("lines").values()) {
                String lineId = StringUtils.substringAfterLast(line.path("id").asString(""), ":");
                disruptionIds(line).forEach(id -> linesByDisruption.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(lineId));
            }
            for (JsonNode stopArea : report.path("stop_areas").values()) {
                String stopAreaId = StringUtils.removeStart(stopArea.path("id").asString(""), "stop_area:");
                disruptionsByStopArea.computeIfAbsent(stopAreaId, k -> new ArrayList<>()).addAll(disruptionIds(stopArea));
            }
        }

        Map<String, Disruption> disruptions = new HashMap<>();
        for (JsonNode node : response.path("disruptions").values()) {
            Disruption disruption = toDisruption(node, List.copyOf(linesByDisruption.getOrDefault(node.path("id").asString(""), Set.of())), now);
            if (disruption != null) {
                disruptions.put(disruption.id(), disruption);
            }
        }

        Map<String, List<Disruption>> lineDisruptions = new HashMap<>();
        disruptions.values().forEach(disruption -> disruption.lineIds()
            .forEach(lineId -> lineDisruptions.computeIfAbsent(lineId, k -> new ArrayList<>()).add(disruption)));
        Map<String, List<Disruption>> stopDisruptions = new HashMap<>();
        disruptionsByStopArea.forEach((stopAreaId, ids) -> ids.stream()
            .map(disruptions::get)
            .filter(Objects::nonNull)
            .distinct()
            .forEach(disruption -> stopDisruptions.computeIfAbsent(stopAreaId, k -> new ArrayList<>()).add(disruption)));

        log.info("Loaded {} disruptions on {} lines in {} ms", disruptions.size(), lineDisruptions.size(),
            Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
        return new Snapshot(now, lineDisruptions, stopDisruptions);
    }

    private static List<String> disruptionIds(JsonNode object) {
        return object.path("links").values().stream()
            .filter(link -> "disruption".equals(link.path("type").asString("")))
            .map(link -> link.path("id").asString(""))
            .toList();
    }

    /** null for past disruptions, upcoming ones starting later than {@link #UPCOMING} and canceled trips */
    private static Disruption toDisruption(JsonNode node, List<String> lineIds, Instant now) {
        String status = node.path("status").asString("");
        boolean active = "active".equals(status);
        if (!active && !"future".equals(status)) {
            return null;
        }
        boolean tripsOnly = node.path("impacted_objects").values().stream()
            .allMatch(object -> "trip".equals(object.path("pt_object").path("embedded_type").asString("")));
        if (tripsOnly) {
            return null;
        }

        // Current period, or the next one (periods are not always in order)
        Instant start = null;
        Instant end = null;
        for (JsonNode period : node.path("application_periods").values()) {
            Instant periodStart = parseTime(period.path("begin").asString(null));
            Instant periodEnd = parseTime(period.path("end").asString(null));
            if ((periodEnd == null || periodEnd.isAfter(now)) && (start == null || periodStart != null && periodStart.isBefore(start))) {
                start = periodStart;
                end = periodEnd;
            }
        }
        if (!active && (start == null || start.isAfter(now.plus(UPCOMING)))) {
            return null;
        }

        boolean elevator = node.path("tags").values().stream().anyMatch(tag -> "Ascenseur".equals(tag.asString("")));
        String cause = StringUtils.trimToNull(node.path("cause").asString(null));
        Category category = elevator ? Category.ELEVATOR : "travaux".equalsIgnoreCase(cause) ? Category.WORKS : Category.TRAFFIC;
        Severity severity = elevator ? Severity.INFO : switch (node.path("severity").path("effect").asString("")) {
            case "NO_SERVICE" -> Severity.BLOCKING;
            case "REDUCED_SERVICE", "SIGNIFICANT_DELAYS", "DETOUR", "MODIFIED_SERVICE", "STOP_MOVED" -> Severity.DISRUPTED;
            default -> Severity.INFO;
        };

        String title = null;
        String message = null;
        for (JsonNode text : node.path("messages").values()) {
            List<String> types = text.path("channel").path("types").values().stream().map(type -> type.asString("")).toList();
            if (title == null && types.contains("title")) {
                title = StringUtils.trimToNull(text.path("text").asString(null));
            } else if (message == null && types.contains("web")) {
                message = toPlainText(text.path("text").asString(null));
            }
        }

        return new Disruption(node.path("id").asString(""), severity, category, title, message, cause, start, end, active,
            parseTime(node.path("updated_at").asString(null)), lineIds);
    }

    /** IDFM web messages are small HTML fragments */
    static String toPlainText(String html) {
        if (html == null) {
            return null;
        }
        String text = TAG.matcher(PARAGRAPH_END.matcher(html).replaceAll("\n")).replaceAll("");
        text = ENTITY.matcher(text).replaceAll(match -> Matcher.quoteReplacement(decodeEntity(match.group(1))));
        return StringUtils.trimToNull(text.lines().map(String::strip).filter(line -> !line.isEmpty()).collect(Collectors.joining("\n\n")));
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

    private record Snapshot(Instant loadedAt, Map<String, List<Disruption>> lineDisruptions, Map<String, List<Disruption>> stopDisruptions) {

        List<Disruption> forLine(String lineId) {
            return lineDisruptions.getOrDefault(lineId, List.of());
        }

        List<Disruption> forStopArea(String stopAreaId) {
            return stopDisruptions.getOrDefault(stopAreaId, List.of());
        }
    }
}
