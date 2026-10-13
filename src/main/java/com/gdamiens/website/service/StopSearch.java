package com.gdamiens.website.service;

import com.gdamiens.website.client.StopZoneClient;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.PlaceResult;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.utils.Geo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stop area search without Navitia: every served stop area of the GTFS in memory (about 15 000) with its town (IDFM
 * stop zones, open data) and its lines. Forgiving: accents, case, hyphens, {@code st} for {@code saint}, words in any
 * order and unfinished, one typo per word (two in long words), the town as an extra word. Ranked by how well the name
 * matches, then importance (heavy modes, number of lines) and closeness to the caller. Built in the background at
 * startup and after each GTFS import (or by the first search that needs it).
 */
@Service
public class StopSearch {

    private static final Logger log = LoggerFactory.getLogger(StopSearch.class);

    /** Without the towns (open data down), built again after this long */
    private static final Duration TOWNS_RETRY = Duration.ofHours(1);

    /** Left out of the query words: "gare de lyon" finds "Gare Lyon" too */
    private static final Set<String> STOP_WORDS = Set.of("de", "du", "des", "la", "le", "les", "d", "l", "et", "a", "au", "aux", "sur", "en");

    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "st", "saint", "ste", "sainte", "pte", "porte", "av", "avenue", "bd", "boulevard", "pl", "place", "gde", "grande");

    private static final Set<TransportMode> HEAVY_MODES = EnumSet.of(TransportMode.METRO, TransportMode.RER, TransportMode.TRANSILIEN,
        TransportMode.TER, TransportMode.TRAM);

    /** A stop area of the index, its name and town cut into normalized words */
    record Entry(String id, String name, String town, double lat, double lon, List<LineSummary> lines, List<String> nameWords,
                 List<String> townWords, boolean heavy) {

        /** With the town, unless the name already says it ({@code Vincennes}, {@code Fontenay-sous-Bois}) */
        String fullName() {
            return town == null || nameWords.containsAll(townWords) ? name : name + " (" + town + ")";
        }
    }

    private record Index(List<Entry> entries, boolean complete, Instant builtAt) {
    }

    private record Scored(Entry entry, double score) {
    }

    private final NetworkRepository networkRepository;

    private final NetworkService networkService;

    private final StopZoneClient stopZoneClient;

    private volatile Index index;

    public StopSearch(NetworkRepository networkRepository, NetworkService networkService, StopZoneClient stopZoneClient) {
        this.networkRepository = networkRepository;
        this.networkService = networkService;
        this.stopZoneClient = stopZoneClient;
    }

    /** Built in the background at startup, so that the first search doesn't wait for it (about 3 s) */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        Thread.ofVirtual().name("stop-search-index").start(() -> {
            try {
                index();
            } catch (RuntimeException e) {
                log.warn("Stop search index not built: {}", e.getMessage());
            }
        });
    }

    @EventListener(GtfsImportedEvent.class)
    public void onGtfsImported() {
        index = null;
        warmUp();
    }

    /**
     * Builds the index again now (towns and lines fetched again), for an administrator
     *
     * @return the stop areas indexed
     */
    public int rebuild() {
        synchronized (this) {
            index = build();
            return index.entries().size();
        }
    }

    /**
     * @param lat the caller's position, to rank the closer stop areas first (null when unknown)
     * @return the best matching stop areas, named with their town ({@code Mairie (Montreuil)})
     */
    public List<PlaceResult> search(String query, Double lat, Double lon, int limit) {
        List<String> all = words(query);
        List<String> required = all.stream().filter(word -> !STOP_WORDS.contains(word)).toList();
        List<String> words = required.isEmpty() ? all : required;
        if (words.isEmpty()) {
            return List.of();
        }
        String fullQuery = String.join(" ", all);
        return index().entries().stream()
            .map(entry -> new Scored(entry, score(entry, words, fullQuery, lat, lon)))
            .filter(scored -> scored.score() > 0)
            .sorted(Comparator.comparingDouble(Scored::score).reversed().thenComparing(scored -> scored.entry().name().length()))
            .limit(limit)
            .map(scored -> new PlaceResult(PlaceResult.Type.STOP_AREA, scored.entry().id(), scored.entry().fullName(), scored.entry().lat(),
                scored.entry().lon(), scored.entry().lines()))
            .toList();
    }

    /**
     * 0 when a query word matches neither the name nor the town. Each word scores 3 when it is a word of the name, 2
     * the start of one, 1 with a typo or in the town; then bonuses for the whole name, heavy modes, lines and closeness.
     */
    static double score(Entry entry, List<String> words, String fullQuery, Double lat, Double lon) {
        int points = 0;
        for (String word : words) {
            int best = 0;
            for (String nameWord : entry.nameWords()) {
                best = Math.max(best, wordMatch(word, nameWord));
                if (best == 3) {
                    break;
                }
            }
            if (best == 0 && entry.townWords().stream().anyMatch(townWord -> townWord.startsWith(word))) {
                best = 1;
            }
            if (best == 0) {
                return 0;
            }
            points += best;
        }
        double score = points / (3.0 * words.size());
        String name = String.join(" ", entry.nameWords());
        if (name.equals(fullQuery)) {
            score += 0.5;
        } else if (name.startsWith(fullQuery)) {
            score += 0.2;
        }
        if (entry.heavy()) {
            score += 0.15;
        }
        score += Math.min(entry.lines().size(), 10) * 0.01;
        if (lat != null && lon != null) {
            double km = Geo.meters(lat, lon, entry.lat(), entry.lon()) / 1000;
            score += 0.2 * Math.max(0, 1 - km / 20);
        }
        return score;
    }

    /** 3: same word, 2: the start of it, 1: one typo (two from 8 letters), from 4 letters, in the word or its start */
    static int wordMatch(String query, String word) {
        if (word.equals(query)) {
            return 3;
        }
        if (word.startsWith(query)) {
            return 2;
        }
        if (query.length() < 4) {
            return 0;
        }
        int allowed = query.length() >= 8 ? 2 : 1;
        if (distance(query, word, allowed) <= allowed
            || word.length() > query.length() && distance(query, word.substring(0, query.length()), allowed) <= allowed) {
            return 1;
        }
        return 0;
    }

    /** Levenshtein distance, anything above [max] reported as max + 1 */
    static int distance(String a, String b, int max) {
        if (Math.abs(a.length() - b.length()) > max) {
            return max + 1;
        }
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            int rowMin = current[0];
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
                rowMin = Math.min(rowMin, current[j]);
            }
            if (rowMin > max) {
                return max + 1;
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return Math.min(previous[b.length()], max + 1);
    }

    /** Normalized words: no accents, lower case, hyphens and apostrophes split, abbreviations written out */
    static List<String> words(String text) {
        return Arrays.stream(SearchService.normalize(text).replaceAll("[-']", " ").split(" "))
            .filter(word -> !word.isEmpty())
            .map(word -> ABBREVIATIONS.getOrDefault(word, word))
            .toList();
    }

    private Index index() {
        Index current = index;
        if (current != null && (current.complete() || current.builtAt().plus(TOWNS_RETRY).isAfter(Instant.now()))) {
            return current;
        }
        synchronized (this) {
            if (index == current) {
                index = build();
            }
            return index;
        }
    }

    private Index build() {
        Map<String, String> towns;
        boolean complete = true;
        try {
            towns = stopZoneClient.getTowns();
        } catch (RuntimeException e) {
            log.warn("Stop towns unavailable, stop areas named without them for now: {}", e.getMessage());
            towns = Map.of();
            complete = false;
        }
        Map<String, String> knownTowns = towns;
        List<Entry> entries = networkRepository.findServedStopAreas().stream()
            .map(row -> {
                List<LineSummary> lines = networkService.toLineSummaries(row.routeIds());
                String town = knownTowns.get(row.id());
                return new Entry(row.id(), row.name(), town, row.lat(), row.lon(), lines, words(row.name()),
                    town == null ? List.of() : words(town), lines.stream().anyMatch(line -> HEAVY_MODES.contains(line.mode())));
            })
            .toList();
        log.info("Stop search index: {} stop areas, {} with their town", entries.size(), entries.stream().filter(entry -> entry.town() != null).count());
        return new Index(entries, complete, Instant.now());
    }
}
