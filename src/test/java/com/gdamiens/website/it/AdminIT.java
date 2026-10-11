package com.gdamiens.website.it;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.client.ApiQuotaStore;
import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.ExternalApis;
import com.gdamiens.website.support.FakeServer.Response;
import com.gdamiens.website.support.IntegrationTest;
import com.gdamiens.website.support.SiriJson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** What supervision reads: quotas, circuit breakers, GTFS imports */
class AdminIT extends IntegrationTest {

    @Autowired
    private ApiQuota apiQuota;

    @Autowired
    private ApiQuotaStore apiQuotaStore;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void describesTheService() {
        JsonNode status = admin().api().get("/api/admin/status").json();

        assertThat(status.path("quotas").values().stream().map(quota -> quota.path("api").asString()))
            .containsExactlyInAnyOrder("STOP_MONITORING", "ESTIMATED_TIMETABLE", "NAVITIA", "DISRUPTIONS_BULK");
        assertThat(status.path("features").values().stream()
            .filter(feature -> "TRAFFIC".equals(feature.path("feature").asString()))
            .map(feature -> feature.path("api").asString())).containsExactly("DISRUPTIONS_BULK");
        assertThat(status.path("prim").values().stream().map(circuit -> circuit.path("state").asString())).containsOnly("CLOSED");
        assertThat(status.path("lastGtfsSuccess").path("status").asString()).isEqualTo("SUCCESS");
        assertThat(status.path("gtfsStale").asBoolean()).isFalse();
    }

    @Test
    void countsThePrimCalls() {
        Api admin = admin().api();
        int before = stopMonitoringUsed(admin);

        // A stop area no other test asks: not cached yet
        admin.get("/api/v2/stops/IDFM:70006/departures");

        assertThat(stopMonitoringUsed(admin)).isEqualTo(before + 1);
    }

    @Test
    void countsTheCallsPerFeature() {
        Api admin = admin().api();
        int before = departuresCalls(admin);

        admin.get("/api/v2/stops/IDFM:70007/departures");

        JsonNode status = admin.get("/api/admin/status").json();
        assertThat(departuresCalls(admin)).isEqualTo(before + 1);
        assertThat(status.path("features").values().stream().map(feature -> feature.path("feature").asString()))
            .contains("DEPARTURES", "VEHICLES", "JOURNEYS", "WALKING_PATHS", "PLACES", "TRAFFIC");
    }

    @Test
    void followsPrimsCount() {
        // The key's other users (a development machine) made 5 000 stop-monitoring calls today
        EXTERNAL.on("/prim/stop-monitoring", ExternalApis.primCount(Response.json(SiriJson.stopMonitoring(List.of())),
            ExternalApis.STOP_MONITORING_PER_DAY, 5_000));
        Api admin = admin().api();

        admin.get("/api/v2/stops/IDFM:70004/departures");

        JsonNode quota = stopMonitoring(admin);
        assertThat(quota.path("primUsed").asInt()).isEqualTo(5_000);
        assertThat(quota.path("primLimit").asInt()).isEqualTo(ExternalApis.STOP_MONITORING_PER_DAY);
        assertThat(quota.path("primObservedAt").isNull()).isFalse();
        assertThat(quota.path("used").asInt()).isGreaterThanOrEqualTo(5_000);
    }

    @Test
    void savesTheCountsForTheNextStart() {
        Api admin = admin().api();
        admin.get("/api/v2/stops/IDFM:70005/departures");
        int calls = departuresCalls(admin);

        apiQuotaStore.save();

        assertThat(jdbc.queryForObject("SELECT calls FROM public.api_quota_usage WHERE day = ? AND feature = 'DEPARTURES'",
            Integer.class, apiQuota.day())).isGreaterThanOrEqualTo(calls);
        assertThat(jdbc.queryForObject("SELECT api FROM public.api_quota_usage WHERE day = ? AND feature = 'DEPARTURES'",
            String.class, apiQuota.day())).isEqualTo("STOP_MONITORING");
    }

    @Test
    void isForAdminsOnly() {
        assertThat(api.get("/api/admin/status").status()).isEqualTo(401);
        assertThat(guest().get("/api/admin/status").status()).isEqualTo(403);
        Api.Response user = newUser().api().get("/api/admin/status");
        assertThat(user.status()).isEqualTo(403);
        assertThat(user.json().path("detail").asString()).isEqualTo("Access denied");
    }

    private static int stopMonitoringUsed(Api admin) {
        return stopMonitoring(admin).path("used").asInt();
    }

    private static JsonNode stopMonitoring(Api admin) {
        return admin.get("/api/admin/status").json().path("quotas").values().stream()
            .filter(quota -> "STOP_MONITORING".equals(quota.path("api").asString()))
            .findFirst().orElseThrow();
    }

    private static int departuresCalls(Api admin) {
        return admin.get("/api/admin/status").json().path("features").values().stream()
            .filter(feature -> "DEPARTURES".equals(feature.path("feature").asString()))
            .findFirst().orElseThrow()
            .path("calls").asInt();
    }
}
