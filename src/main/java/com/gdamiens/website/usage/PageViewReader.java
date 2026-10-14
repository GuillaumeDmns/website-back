package com.gdamiens.website.usage;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.utils.ParisTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Visits of the static sites (guillaumedamiens.com, the web app, the administration) from the log of their page loads
 * written by nginx: one line per request, {@code time host method uri status ip referrer user-agent} separated by
 * tabs. Every 5 minutes the new lines are read (from where the last read stopped, the rotated file first after a
 * rotation) and counted per Paris day: views per page ({@code usage_page_daily}), distinct visitors per site (IP and
 * browser as a keyed hash of the day, {@link UsageRecorder}), referring sites ({@code usage_referrer_daily}, their
 * host only). Robots, assets and failed requests are left out. Nothing else of the log is kept.
 */
@Component
public class PageViewReader {

    private static final Logger log = LoggerFactory.getLogger(PageViewReader.class);

    private static final String CURSOR = "pages";

    /** Read at most this much per pass (a few days of logs) */
    private static final int MAX_BYTES = 64 * 1024 * 1024;

    private static final Pattern ROBOT = Pattern.compile(
        "(?i).*(bot|crawl|spider|slurp|curl|wget|python|go-http|java/|okhttp|axios|node-fetch|headless|scan|monitor|uptime|"
            + "preview|facebookexternalhit|httpclient|libwww|lighthouse|research|zgrab|masscan|nmap|nikto).*");

    /** Not pages: files of the sites and probes for other software */
    private static final Set<String> FILE_PREFIXES = Set.of("/api/", "/assets/", "/canvaskit/", "/icons/", "/.well-known/", "/photos/");

    private static final String OWN_DOMAIN = "guillaumedamiens.com";

    /** A line of the log that counts as a page view */
    record PageView(Instant at, String host, String path, String visitor, String referrer) {
    }

    private record Cursor(long inode, long position) {
    }

    private final ApplicationProperties applicationProperties;

    private final UsageRecorder recorder;

    private final NamedParameterJdbcTemplate jdbc;

    private final TransactionTemplate transaction;

    public PageViewReader(ApplicationProperties applicationProperties, UsageRecorder recorder, NamedParameterJdbcTemplate jdbc,
                          TransactionTemplate transaction) {
        this.applicationProperties = applicationProperties;
        this.recorder = recorder;
        this.jdbc = jdbc;
        this.transaction = transaction;
    }

    @Scheduled(initialDelay = 1, fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void scheduledRead() {
        String pagesLog = applicationProperties.getPagesLog();
        if (pagesLog == null || pagesLog.isBlank()) {
            return;
        }
        try {
            read(Path.of(pagesLog));
        } catch (IOException | DataAccessException e) {
            log.warn("Page log {} not read: {}", pagesLog, e.getMessage());
        }
    }

    /**
     * Reads the lines added to [log] since the last read
     *
     * @return the page views counted
     */
    public synchronized int read(Path log) throws IOException {
        if (!Files.exists(log)) {
            return 0;
        }
        long inode = inode(log);
        Cursor cursor = cursor();
        Map<List<Object>, Integer> views = new HashMap<>();
        Map<List<Object>, Integer> referrers = new HashMap<>();
        int counted = 0;
        long start = 0;
        if (cursor != null && cursor.inode() == inode) {
            start = Files.size(log) < cursor.position() ? 0 : cursor.position();
        } else if (cursor != null) {
            // Rotated since: the end of the previous file first (logrotate renames it to .1, then nginx reopens a new one)
            Path rotated = log.resolveSibling(log.getFileName() + ".1");
            if (Files.exists(rotated) && inode(rotated) == cursor.inode()) {
                counted += countLines(rotated, cursor.position(), views, referrers).views();
            }
        }
        Read read = countLines(log, start, views, referrers);
        save(views, referrers, new Cursor(inode, read.position()));
        return counted + read.views();
    }

    /** What a read found: page views, and the position after the last complete line */
    private record Read(int views, long position) {
    }

    private Read countLines(Path file, long from, Map<List<Object>, Integer> views, Map<List<Object>, Integer> referrers) throws IOException {
        int[] counted = new int[1];
        long position = readLines(file, from, line -> {
            PageView view = parse(line);
            if (view != null) {
                count(view, views, referrers);
                counted[0]++;
            }
        });
        return new Read(counted[0], position);
    }

    private void count(PageView view, Map<List<Object>, Integer> views, Map<List<Object>, Integer> referrers) {
        LocalDate day = LocalDate.ofInstant(view.at(), ParisTime.ZONE);
        views.merge(List.of(day, view.host(), view.path()), 1, Integer::sum);
        if (view.referrer() != null) {
            referrers.merge(List.of(day, view.host(), view.referrer()), 1, Integer::sum);
        }
        recorder.member("visitors:" + view.host(), view.visitor(), view.at());
    }

    private interface LineHandler {
        void line(String line);
    }

    /** Complete lines from [from] on (a line being written is left for next time); the position after the last one */
    private static long readLines(Path file, long from, LineHandler handler) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
            channel.position(from);
            long size = channel.size();
            int length = (int) Math.min(Math.max(size - from, 0), MAX_BYTES);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            while (buffer.hasRemaining() && channel.read(buffer) > 0) {
                // Reads up to the end
            }
            byte[] bytes = buffer.array();
            int lineStart = 0;
            for (int i = 0; i < buffer.position(); i++) {
                if (bytes[i] == '\n') {
                    handler.line(new String(bytes, lineStart, i - lineStart, StandardCharsets.UTF_8));
                    lineStart = i + 1;
                }
            }
            return from + lineStart;
        }
    }

    /** A page view of one of the sites, or null (a file, a robot, a failure, another site, a malformed line) */
    static PageView parse(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length < 8) {
            return null;
        }
        String host = fields[1].toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "").replaceFirst(":\\d+$", "");
        String method = fields[2];
        String path = fields[3];
        String status = fields[4];
        String userAgent = fields[7];
        if (!(host.equals(OWN_DOMAIN) || host.endsWith("." + OWN_DOMAIN)) || !"GET".equals(method)
            || !(status.equals("200") || status.equals("304")) || !isPage(path) || isRobot(userAgent)) {
            return null;
        }
        Instant at;
        try {
            at = OffsetDateTime.parse(fields[0]).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
        return new PageView(at, host, path.length() > 200 ? path.substring(0, 200) : path, fields[5] + "|" + userAgent, referrer(fields[6]));
    }

    /** A page: no extension (the sites' routes) or .html, outside the folders of files */
    static boolean isPage(String path) {
        if (!path.startsWith("/") || path.contains("..") || FILE_PREFIXES.stream().anyMatch(path::startsWith)) {
            return false;
        }
        String last = path.substring(path.lastIndexOf('/') + 1);
        return !last.contains(".") || last.endsWith(".html");
    }

    static boolean isRobot(String userAgent) {
        return userAgent == null || userAgent.isBlank() || "-".equals(userAgent) || !userAgent.startsWith("Mozilla/")
            || ROBOT.matcher(userAgent).matches();
    }

    /** The referring site's host ({@code www.google.com} → {@code google.com}), null for none or our own sites */
    static String referrer(String referrer) {
        if (referrer == null || referrer.isBlank() || "-".equals(referrer)) {
            return null;
        }
        try {
            String host = URI.create(referrer).getHost();
            if (host == null) {
                return null;
            }
            host = host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
            if (host.equals(OWN_DOMAIN) || host.endsWith("." + OWN_DOMAIN)) {
                return null;
            }
            return host.length() > 100 ? host.substring(0, 100) : host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Cursor cursor() {
        return jdbc.query("SELECT inode, position FROM public.usage_log_cursor WHERE name = :name", new MapSqlParameterSource("name", CURSOR),
            rs -> rs.next() ? new Cursor(rs.getLong("inode"), rs.getLong("position")) : null);
    }

    private void save(Map<List<Object>, Integer> views, Map<List<Object>, Integer> referrers, Cursor cursor) {
        transaction.executeWithoutResult(status -> {
            views.forEach((key, count) -> jdbc.update("""
                    INSERT INTO public.usage_page_daily (day, host, path, views) VALUES (:day, :host, :path, :views)
                    ON CONFLICT (day, host, path) DO UPDATE SET views = usage_page_daily.views + EXCLUDED.views""",
                new MapSqlParameterSource().addValue("day", key.get(0)).addValue("host", key.get(1)).addValue("path", key.get(2))
                    .addValue("views", count)));
            referrers.forEach((key, count) -> jdbc.update("""
                    INSERT INTO public.usage_referrer_daily (day, host, referrer, views) VALUES (:day, :host, :referrer, :views)
                    ON CONFLICT (day, host, referrer) DO UPDATE SET views = usage_referrer_daily.views + EXCLUDED.views""",
                new MapSqlParameterSource().addValue("day", key.get(0)).addValue("host", key.get(1)).addValue("referrer", key.get(2))
                    .addValue("views", count)));
            jdbc.update("""
                    INSERT INTO public.usage_log_cursor (name, inode, position, updated_at) VALUES (:name, :inode, :position, now())
                    ON CONFLICT (name) DO UPDATE SET inode = EXCLUDED.inode, position = EXCLUDED.position, updated_at = now()""",
                new MapSqlParameterSource().addValue("name", CURSOR).addValue("inode", cursor.inode()).addValue("position", cursor.position()));
        });
    }

    private static long inode(Path file) throws IOException {
        try {
            return ((Number) Files.getAttribute(file, "unix:ino")).longValue();
        } catch (UnsupportedOperationException e) {
            return 0;
        }
    }
}
