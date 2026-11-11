package com.gdamiens.website.usage;

import org.springframework.web.util.pattern.PathPattern;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsageFilterTest {

    private static final List<PathPattern> ROUTES = UsageFilter.parse(List.of("/api/v2/stops/{stopAreaId}",
        "/api/v2/stops/{stopAreaId}/departures", "/api/v2/lines/{lineId}/vehicles", "/api/auth/guest", "/api/v2/me/favorites/{id}"));

    @Test
    void namesTheRouteAsSpringMatchedIt() {
        assertThat(UsageFilter.endpoint("GET", "/api/v2/stops/{stopAreaId}/departures", "/api/v2/stops/IDFM:71264/departures", ROUTES))
            .isEqualTo("GET /api/v2/stops/{stopAreaId}/departures");
    }

    @Test
    void namesARequestThatReachedNoControllerAfterItsRoute() {
        assertThat(UsageFilter.endpoint("GET", null, "/api/v2/stops/IDFM:71264/departures", ROUTES))
            .isEqualTo("GET /api/v2/stops/{stopAreaId}/departures");
        assertThat(UsageFilter.endpoint("GET", null, "/api/v2/lines/C01371/vehicles", ROUTES)).isEqualTo("GET /api/v2/lines/{lineId}/vehicles");
        assertThat(UsageFilter.endpoint("POST", null, "/api/auth/guest", ROUTES)).isEqualTo("POST /api/auth/guest");
    }

    @Test
    void countsMadeUpPathsTogether() {
        // Anyone can ask for as many paths as they like: none becomes a row of the statistics
        assertThat(UsageFilter.endpoint("GET", null, "/api/" + "x".repeat(300), ROUTES)).isEqualTo("GET (unknown)");
        assertThat(UsageFilter.endpoint("GET", null, "/api/v2/stops/IDFM:71264/made-up-" + System.nanoTime(), ROUTES)).isEqualTo("GET (unknown)");
        assertThat(UsageFilter.endpoint("GET", null, "/api/wp-login.php", ROUTES)).isEqualTo("GET (unknown)");
        assertThat(UsageFilter.endpoint("GET", null, "/api/%zz/..//", ROUTES)).isEqualTo("GET (unknown)");
    }

    @Test
    void keepsOnlyPlainHeaderValues() {
        assertThat(UsageFilter.headerValue(" Android ")).isEqualTo("android");
        assertThat(UsageFilter.headerValue("0.4.0-12")).isEqualTo("0.4.0-12");
        assertThat(UsageFilter.headerValue("<script>")).isNull();
        assertThat(UsageFilter.headerValue("x".repeat(30))).isNull();
        assertThat(UsageFilter.headerValue(null)).isNull();
    }

    @Test
    void groupsTheStatuses() {
        assertThat(UsageRecorder.statusClass(200)).isEqualTo("2xx");
        assertThat(UsageRecorder.statusClass(304)).isEqualTo("3xx");
        assertThat(UsageRecorder.statusClass(404)).isEqualTo("4xx");
        assertThat(UsageRecorder.statusClass(429)).isEqualTo("429");
        assertThat(UsageRecorder.statusClass(503)).isEqualTo("5xx");
    }

    @Test
    void hashesWithTheDaysSalt() {
        byte[] monday = "salt of monday 0123456789abcdef".getBytes();
        byte[] tuesday = "salt of tuesday 0123456789abcde".getBytes();

        assertThat(UsageRecorder.hash(monday, "guest:42")).hasSize(8).isEqualTo(UsageRecorder.hash(monday, "guest:42"));
        assertThat(UsageRecorder.hash(monday, "guest:42")).isNotEqualTo(UsageRecorder.hash(monday, "guest:43"));
        // Another day, another hash: days can't be linked
        assertThat(UsageRecorder.hash(tuesday, "guest:42")).isNotEqualTo(UsageRecorder.hash(monday, "guest:42"));
    }
}
