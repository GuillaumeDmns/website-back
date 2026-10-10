package com.gdamiens.website.it;

import com.gdamiens.website.support.Api;
import com.gdamiens.website.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** What supervision reads: quotas, circuit breakers, GTFS imports */
class AdminIT extends IntegrationTest {

    @Test
    void describesTheService() {
        JsonNode status = admin().api().get("/api/admin/status").json();

        assertThat(status.path("quotas").values().stream().map(quota -> quota.path("api").asString()))
            .containsExactlyInAnyOrder("STOP_MONITORING", "ESTIMATED_TIMETABLE", "NAVITIA");
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
    void isForAdminsOnly() {
        assertThat(api.get("/api/admin/status").status()).isEqualTo(401);
        assertThat(guest().get("/api/admin/status").status()).isEqualTo(403);
        Api.Response user = newUser().api().get("/api/admin/status");
        assertThat(user.status()).isEqualTo(403);
        assertThat(user.json().path("detail").asString()).isEqualTo("Access denied");
    }

    private static int stopMonitoringUsed(Api admin) {
        return admin.get("/api/admin/status").json().path("quotas").values().stream()
            .filter(quota -> "STOP_MONITORING".equals(quota.path("api").asString()))
            .findFirst().orElseThrow()
            .path("used").asInt();
    }
}
