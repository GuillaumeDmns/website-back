package com.gdamiens.website.security;

import com.gdamiens.website.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityEventsTest {

    // 23:30 in Paris (winter time)
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-12T22:30:00Z"));

    private final SecurityEvents events = new SecurityEvents(clock);

    private SecurityEvents.Count count(SecurityEvents.Kind kind) {
        return events.counts().stream().filter(count -> count.kind() == kind).findFirst().orElseThrow();
    }

    @Test
    void countsEachKindTodayAndSinceTheStart() {
        events.record(SecurityEvents.Kind.ACCESS_DENIED);
        events.record(SecurityEvents.Kind.ACCESS_DENIED);
        events.record(SecurityEvents.Kind.JWT_REFUSED);

        assertThat(count(SecurityEvents.Kind.ACCESS_DENIED)).isEqualTo(new SecurityEvents.Count(SecurityEvents.Kind.ACCESS_DENIED, 2, 2));
        assertThat(count(SecurityEvents.Kind.REFRESH_TOKEN_REUSED).sinceStart()).isZero();
        // Every kind is listed, even without any
        assertThat(events.counts()).hasSize(SecurityEvents.Kind.values().length);
    }

    @Test
    void todayStartsAtMidnightInParis() {
        events.record(SecurityEvents.Kind.GOOGLE_TOKEN_REFUSED);

        clock.advance(Duration.ofMinutes(45));
        events.record(SecurityEvents.Kind.GOOGLE_TOKEN_REFUSED);

        assertThat(count(SecurityEvents.Kind.GOOGLE_TOKEN_REFUSED).today()).isEqualTo(1);
        assertThat(count(SecurityEvents.Kind.GOOGLE_TOKEN_REFUSED).sinceStart()).isEqualTo(2);
    }

    @Test
    void keepsTheLastAdminActionsMostRecentFirst() {
        IntStream.range(0, SecurityEvents.MAX_ACTIONS + 5).forEach(i -> events.adminAction("google:1", "traffic", i, null));
        events.adminAction("google:1", "search-index", null, "IllegalStateException");

        assertThat(events.adminActions()).hasSize(SecurityEvents.MAX_ACTIONS);
        assertThat(events.adminActions().getFirst().action()).isEqualTo("search-index");
        assertThat(events.adminActions().getFirst().error()).isEqualTo("IllegalStateException");
        assertThat(events.adminActions().get(1).count()).isEqualTo(SecurityEvents.MAX_ACTIONS + 4);
    }
}
