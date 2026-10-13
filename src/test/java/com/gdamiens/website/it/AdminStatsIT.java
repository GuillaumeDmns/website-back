package com.gdamiens.website.it;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.IntegrationTest;
import com.gdamiens.website.support.MiniGtfs;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** What the administration (website-admin) shows and does. The database is shared with the other tests: counts are at least */
class AdminStatsIT extends IntegrationTest {

    private static final String TODAY = LocalDate.now(ZoneId.of("Europe/Paris")).toString();

    @Test
    void countsTheAccounts() {
        newUser();
        newUser();

        JsonNode accounts = admin().api().get("/api/admin/stats/accounts?days=30").json();

        JsonNode counts = accounts.path("counts");
        assertThat(counts.path("total").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(counts.path("admins").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(counts.path("activeToday").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(counts.path("new7Days").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(counts.path("withSession").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(dayCount(accounts.path("signUps"), TODAY)).isGreaterThanOrEqualTo(3);
        assertThat(dayCount(accounts.path("activePerDay"), TODAY)).isGreaterThanOrEqualTo(3);
        assertThat(groupCount(accounts.path("lastSignIns"), "today")).isGreaterThanOrEqualTo(3);
        assertThat(groupCount(accounts.path("favoritesPerAccount"), "0")).isGreaterThanOrEqualTo(1);
    }

    @Test
    void neverTellsWhoTheAccountsAre() {
        SignedIn user = signInWithGoogle("someone-" + System.nanoTime() + "@example.com", "Someone");
        user.api().post("/api/v2/me/favorites", Map.of("kind", "HOME", "label", "Home", "lat", 48.85, "lon", 2.35));
        Api admin = admin().api();

        String body = admin.get("/api/admin/stats/accounts").body() + admin.get("/api/admin/stats/favorites").body();

        assertThat(body).doesNotContain("someone-", "Someone", user.googleSub(), "48.85");
    }

    @Test
    void ranksTheFavorites() {
        for (int i = 0; i < 3; i++) {
            Api user = newUser().api();
            user.post("/api/v2/me/favorites", Map.of("kind", "STOP", "stopAreaId", MiniGtfs.NATION));
            user.post("/api/v2/me/favorites", Map.of("kind", "LINE", "lineId", MiniGtfs.METRO_1));
        }

        JsonNode favorites = admin().api().get("/api/admin/stats/favorites").json();

        JsonNode nation = favorites.path("stops").values().stream()
            .filter(stop -> MiniGtfs.NATION.equals(stop.path("id").asString())).findFirst().orElseThrow();
        assertThat(nation.path("name").asString()).isEqualTo("Nation");
        assertThat(nation.path("count").asInt()).isGreaterThanOrEqualTo(3);
        JsonNode metro = favorites.path("lines").values().stream()
            .filter(line -> MiniGtfs.METRO_1.equals(line.path("line").path("id").asString())).findFirst().orElseThrow();
        assertThat(metro.path("line").path("name").asString()).isEqualTo("1");
        assertThat(metro.path("count").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(groupCount(favorites.path("perKind"), "STOP")).isGreaterThanOrEqualTo(3);
        assertThat(favorites.path("accounts").asInt()).isGreaterThanOrEqualTo(3);
        assertThat(dayCount(favorites.path("created"), TODAY)).isGreaterThanOrEqualTo(6);
    }

    @Test
    void givesThePrimCallsPerDay() {
        Api admin = admin().api();
        int departures = admin.get("/api/admin/status").json().path("features").values().stream()
            .filter(feature -> "DEPARTURES".equals(feature.path("feature").asString()))
            .findFirst().orElseThrow().path("calls").asInt();

        JsonNode quotas = admin.get("/api/admin/stats/quotas?days=7").json();

        assertThat(quotas.path("apis").values().stream().map(api -> api.path("api").asString()))
            .containsExactlyInAnyOrder("STOP_MONITORING", "ESTIMATED_TIMETABLE", "NAVITIA", "DISRUPTIONS_BULK");
        JsonNode stopMonitoring = quotas.path("apis").values().stream()
            .filter(api -> "STOP_MONITORING".equals(api.path("api").asString())).findFirst().orElseThrow();
        assertThat(stopMonitoring.path("perDay").asInt()).isEqualTo(ApiQuota.Api.STOP_MONITORING.perDay());
        assertThat(stopMonitoring.path("limit").asInt()).isLessThan(ApiQuota.Api.STOP_MONITORING.perDay());
        // Today's from the live counters
        assertThat(quotas.path("days").values().stream()
            .filter(day -> TODAY.equals(day.path("day").asString()) && "DEPARTURES".equals(day.path("feature").asString()))
            .mapToInt(day -> day.path("calls").asInt()).sum()).isGreaterThanOrEqualTo(departures);
    }

    @Test
    void describesTheNetworkAndTheDatabase() {
        JsonNode network = admin().api().get("/api/admin/stats/network").json();

        assertThat(network.path("linesPerMode").path("METRO").asInt()).isEqualTo(1);
        assertThat(network.path("linesPerMode").path("RER").asInt()).isEqualTo(1);
        assertThat(network.path("stopAreas").asInt()).isGreaterThan(0);
        assertThat(network.path("quays").asInt()).isGreaterThan(0);
        assertThat(network.path("calendar").path("from").isNull()).isFalse();
        assertThat(network.path("importedVersion").asString()).isEqualTo(GTFS_VERSION.toString());
        assertThat(network.path("publishedVersion").asString()).isEqualTo(GTFS_VERSION.toString());
        assertThat(network.path("database").path("bytes").asLong()).isGreaterThan(0);
        assertThat(network.path("database").path("postgis").asString()).isNotEmpty();
        assertThat(network.path("tables").values().stream().map(table -> table.path("schema").asString() + "." + table.path("name").asString()))
            .contains("gtfs.stops", "public.user");
    }

    @Test
    void describesTheMachine() {
        JsonNode system = admin().api().get("/api/admin/stats/system").json();

        assertThat(system.path("java").asString()).isNotEmpty();
        assertThat(system.path("springBoot").asString()).startsWith("4.");
        assertThat(system.path("heapUsed").asLong()).isGreaterThan(0);
        assertThat(system.path("processors").asInt()).isGreaterThan(0);
        assertThat(system.path("diskTotal").asLong()).isGreaterThan(0);
    }

    @Test
    void checksThePublicPages() {
        EXTERNAL.on("/site/", Response.json("{}"));
        EXTERNAL.on("/site/down", Response.status(503));

        JsonNode sites = admin().api().get("/api/admin/stats/sites").json().path("sites");

        assertThat(sites.values().stream().map(site -> site.path("status").asInt())).containsExactly(200, 503);
        assertThat(sites.get(0).path("millis").isNumber()).isTrue();
        // Over HTTP: no certificate
        assertThat(sites.get(0).path("certificateExpiry").isNull()).isTrue();
    }

    @Test
    void fetchesTheTrafficNow() {
        Api admin = admin().api();

        JsonNode result = admin.post("/api/admin/actions/traffic").json();

        assertThat(result.path("at").asString()).isEqualTo(admin.get("/api/admin/status").json().path("trafficSnapshotAt").asString());
        assertThat(admin.post("/api/admin/actions/elevators").status()).isEqualTo(200);
    }

    @Test
    void buildsTheSearchIndexAgain() {
        Api admin = admin().api();

        JsonNode result = admin.post("/api/admin/actions/search-index").json();

        assertThat(result.path("count").asInt()).isGreaterThan(0);
        assertThat(admin.get("/api/v2/search?q=nation").json().toString()).contains("Nation");
    }

    @Test
    void closesThePrimCircuits() {
        circuitBreaker.failure(ApiQuota.Api.NAVITIA, "429 Too Many Requests", true);

        JsonNode circuits = admin().api().post("/api/admin/actions/prim-circuits").json();

        assertThat(circuits.values().stream().map(circuit -> circuit.path("state").asString())).containsOnly("CLOSED");
    }

    @Test
    void deletesTheObsoleteSessions() {
        JsonNode result = admin().api().post("/api/admin/actions/sessions-cleanup").json();

        assertThat(result.path("count").isNumber()).isTrue();
    }

    @Test
    void isForAdminsOnly() {
        Api user = newUser().api();
        for (String path : List.of("/api/admin/stats/accounts", "/api/admin/stats/favorites", "/api/admin/stats/quotas",
            "/api/admin/stats/network", "/api/admin/stats/system", "/api/admin/stats/sites")) {
            assertThat(api.get(path).status()).as(path).isEqualTo(401);
            assertThat(user.get(path).status()).as(path).isEqualTo(403);
        }
        for (String path : List.of("/api/admin/actions/traffic", "/api/admin/actions/elevators", "/api/admin/actions/search-index",
            "/api/admin/actions/prim-circuits", "/api/admin/actions/sessions-cleanup")) {
            assertThat(user.post(path).status()).as(path).isEqualTo(403);
        }
    }

    private static int dayCount(JsonNode days, String day) {
        return days.values().stream().filter(entry -> day.equals(entry.path("day").asString())).mapToInt(entry -> entry.path("count").asInt()).sum();
    }

    private static int groupCount(JsonNode groups, String group) {
        return groups.values().stream().filter(entry -> group.equals(entry.path("group").asString())).mapToInt(entry -> entry.path("count").asInt()).sum();
    }
}
