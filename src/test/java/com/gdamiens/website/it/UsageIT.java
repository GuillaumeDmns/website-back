package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import com.gdamiens.website.support.MiniGtfs;
import com.gdamiens.website.usage.PageViewReader;
import com.gdamiens.website.usage.UsageFilter;
import com.gdamiens.website.usage.UsageRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Anonymous usage statistics: API requests, active devices and versions, visits of the sites read from nginx's log */
class UsageIT extends IntegrationTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private static final String TODAY = LocalDate.now(PARIS).toString();

    private static final String FIREFOX = "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0";

    @Autowired
    private UsageRecorder usageRecorder;

    @Autowired
    private PageViewReader pageViewReader;

    @Autowired
    private JdbcTemplate jdbc;

    @TempDir
    private Path logs;

    @Test
    void countsTheRequestsPerRouteCallerAndStatus() {
        Api guest = guest().withHeader(UsageFilter.PLATFORM_HEADER, "android").withHeader(UsageFilter.VERSION_HEADER, "0.4.0");
        guest.get("/api/v2/lines/" + MiniGtfs.METRO_1);
        guest.get("/api/v2/lines/" + MiniGtfs.METRO_1);
        guest.get("/api/v2/lines/C99999");

        JsonNode usage = admin().api().get("/api/admin/stats/usage?days=1").json();

        JsonNode line = endpoint(usage, "GET /api/v2/lines/{lineId}");
        assertThat(line.path("requests").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(line.path("clientErrors").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(line.path("totalMillis").asLong()).isGreaterThanOrEqualTo(0);
        assertThat(usage.path("callers").values().stream()
            .filter(caller -> TODAY.equals(caller.path("day").asString()) && "GUEST".equals(caller.path("caller").asString()))
            .mapToInt(caller -> caller.path("requests").asInt()).sum()).isGreaterThanOrEqualTo(3);
        assertThat(usage.path("perHour").values().stream().mapToInt(hour -> hour.path("requests").asInt()).sum()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void countsTheRateLimitingsRefusals() {
        Api api = this.api.from("198.51.100." + (int) (Math.random() * 200));
        for (int i = 0; i < 15; i++) {
            api.post("/api/auth/guest");
        }

        JsonNode guestTokens = endpoint(admin().api().get("/api/admin/stats/usage?days=1").json(), "POST /api/auth/guest");

        assertThat(guestTokens.path("rateLimited").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void countsTheDevicesOfTheDayPerPlatformAndVersion() {
        String version = "9." + (int) (Math.random() * 90) + ".0";
        for (int i = 0; i < 2; i++) {
            Api device = guest().withHeader(UsageFilter.PLATFORM_HEADER, "android").withHeader(UsageFilter.VERSION_HEADER, version);
            device.get("/api/v2/traffic");
            device.get("/api/v2/traffic");
        }
        newUser().api().withHeader(UsageFilter.PLATFORM_HEADER, "web").get("/api/v2/me");

        JsonNode audience = admin().api().get("/api/admin/stats/audience?days=1").json();

        // Each device once, however many requests it made
        assertThat(metric(audience, "version:" + version)).isEqualTo(2);
        assertThat(metric(audience, "active:guest")).isGreaterThanOrEqualTo(2);
        assertThat(metric(audience, "active:account")).isGreaterThanOrEqualTo(1);
        assertThat(metric(audience, "platform:web")).isGreaterThanOrEqualTo(1);
    }

    @Test
    void closesTheDaysAndForgetsWhoWasCounted() {
        java.time.Instant yesterday = ZonedDateTime.now(PARIS).minusDays(1).toInstant();
        String metric = "visitors:test-" + UUID.randomUUID().toString().substring(0, 8);
        usageRecorder.member(metric, "visitor-1", yesterday);
        usageRecorder.member(metric, "visitor-2", yesterday);
        usageRecorder.member(metric, "visitor-1", yesterday);

        usageRecorder.closeDays();

        LocalDate day = LocalDate.now(PARIS).minusDays(1);
        assertThat(jdbc.queryForObject("SELECT count FROM public.usage_daily WHERE day = ? AND metric = ?", Integer.class, day, metric)).isEqualTo(2);
        // Neither the hashes nor the salt of a closed day remain
        assertThat(jdbc.queryForObject("SELECT count(*) FROM public.usage_member WHERE day < ?", Integer.class, LocalDate.now(PARIS))).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM public.usage_salt WHERE day < ?", Integer.class, LocalDate.now(PARIS))).isZero();
    }

    @Test
    void readsTheVisitsOfTheSitesAcrossARotationOfTheLog() throws Exception {
        Path log = logs.resolve("pages.log");
        String now = ZonedDateTime.now(PARIS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String page = "/test-" + UUID.randomUUID().toString().substring(0, 8);
        Files.writeString(log, String.join("", List.of(
            line(now, "guillaumedamiens.com", page, "203.0.113.1", "https://www.linkedin.com/in/x", FIREFOX),
            line(now, "guillaumedamiens.com", page, "203.0.113.1", "-", FIREFOX),
            line(now, "guillaumedamiens.com", page, "203.0.113.2", "-", "Mozilla/5.0 (compatible; bingbot/2.0)"),
            line(now, "guillaumedamiens.com", "/assets/index.js", "203.0.113.2", "-", FIREFOX))));
        // A line being written: read next time
        Files.writeString(log, now + "\tguillaumedamiens.com\tGET\t" + page, java.nio.file.StandardOpenOption.APPEND);

        assertThat(pageViewReader.read(log)).isEqualTo(2);

        Files.writeString(log, "\t200\t203.0.113.3\t-\t" + FIREFOX + "\n", java.nio.file.StandardOpenOption.APPEND);
        // logrotate renames the log, nginx starts a new one
        Files.move(log, logs.resolve("pages.log.1"));
        Files.writeString(log, line(now, "app.guillaumedamiens.com", "/", "203.0.113.4", "-", FIREFOX));

        assertThat(pageViewReader.read(log)).isEqualTo(2);
        assertThat(pageViewReader.read(log)).isZero();

        JsonNode audience = admin().api().get("/api/admin/stats/audience?days=1").json();
        assertThat(audience.path("pages").values().stream()
            .filter(view -> page.equals(view.path("path").asString())).findFirst().orElseThrow().path("views").asInt()).isEqualTo(3);
        assertThat(audience.path("referrers").values().stream().map(view -> view.path("referrer").asString())).contains("linkedin.com");
        assertThat(metric(audience, "visitors:guillaumedamiens.com")).isGreaterThanOrEqualTo(2);
        assertThat(audience.path("pagesReadAt").isNull()).isFalse();
        // Counts only: no IP nor browser
        assertThat(audience.toString()).doesNotContain("203.0.113", "Firefox");
    }

    @Test
    void isForAdminsOnly() {
        for (String path : List.of("/api/admin/stats/usage", "/api/admin/stats/audience")) {
            assertThat(api.get(path).status()).as(path).isEqualTo(401);
            assertThat(newUser().api().get(path).status()).as(path).isEqualTo(403);
        }
    }

    private static String line(String time, String host, String uri, String ip, String referrer, String userAgent) {
        return String.join("\t", time, host, "GET", uri, "200", ip, referrer, userAgent) + "\n";
    }

    private static JsonNode endpoint(JsonNode usage, String endpoint) {
        return usage.path("endpoints").values().stream().filter(entry -> endpoint.equals(entry.path("endpoint").asString()))
            .findFirst().orElseThrow(() -> new AssertionError(endpoint + " not in " + usage.path("endpoints")));
    }

    /** A metric of today: the sum of its rows (a day may have a closed count and late members) */
    private static int metric(JsonNode audience, String metric) {
        return audience.path("metrics").values().stream()
            .filter(entry -> TODAY.equals(entry.path("day").asString()) && metric.equals(entry.path("metric").asString()))
            .mapToInt(entry -> entry.path("count").asInt()).sum();
    }
}
