package com.gdamiens.website.usage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsageFilterTest {

    @Test
    void namesTheRouteAsSpringMatchedIt() {
        assertThat(UsageFilter.endpoint("GET", "/api/v2/stops/{stopAreaId}/departures", "/api/v2/stops/IDFM:71264/departures"))
            .isEqualTo("GET /api/v2/stops/{stopAreaId}/departures");
    }

    @Test
    void hidesTheIdsOfARequestThatReachedNoController() {
        assertThat(UsageFilter.endpoint("GET", null, "/api/v2/stops/IDFM:71264/departures")).isEqualTo("GET /api/v2/stops/{id}/departures");
        assertThat(UsageFilter.endpoint("GET", null, "/api/v2/lines/C01371/vehicles")).isEqualTo("GET /api/v2/lines/{id}/vehicles");
        assertThat(UsageFilter.endpoint("POST", null, "/api/auth/guest")).isEqualTo("POST /api/auth/guest");
        assertThat(UsageFilter.endpoint("GET", null, "/api/" + "x".repeat(300))).hasSize(120);
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
