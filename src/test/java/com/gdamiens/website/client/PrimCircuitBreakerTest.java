package com.gdamiens.website.client;

import com.gdamiens.website.client.PrimCircuitBreaker.State;
import com.gdamiens.website.exceptions.PrimUnavailableException;
import com.gdamiens.website.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimCircuitBreakerTest {

    private static final ApiQuota.Api API = ApiQuota.Api.STOP_MONITORING;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-10T08:00:00Z"));

    private final PrimCircuitBreaker breaker = new PrimCircuitBreaker(clock);

    @Test
    void staysClosedBelowTheFailureThreshold() {
        fail(PrimCircuitBreaker.FAILURES - 1);

        assertThatNoException().isThrownBy(() -> breaker.beforeCall(API));
        assertThat(status().state()).isEqualTo(State.CLOSED);
    }

    @Test
    void opensAfterFailuresInARow() {
        fail(PrimCircuitBreaker.FAILURES);

        assertThat(status().state()).isEqualTo(State.OPEN);
        assertThat(status().lastError()).isEqualTo("timeout");
        assertThatThrownBy(() -> breaker.beforeCall(API)).isInstanceOf(PrimUnavailableException.class);
    }

    @Test
    void aSuccessResetsTheFailures() {
        fail(PrimCircuitBreaker.FAILURES - 1);
        breaker.beforeCall(API);
        breaker.success(API);
        fail(PrimCircuitBreaker.FAILURES - 1);

        assertThat(status().state()).isEqualTo(State.CLOSED);
    }

    @Test
    void letsOneCallTryAgainOnceTheDelayIsOver() {
        fail(PrimCircuitBreaker.FAILURES);
        clock.advance(PrimCircuitBreaker.OPEN_FOR.plusSeconds(1));

        breaker.beforeCall(API);
        // Others wait while that call tries
        assertThatThrownBy(() -> breaker.beforeCall(API)).isInstanceOf(PrimUnavailableException.class);
        assertThat(status().state()).isEqualTo(State.TRIAL);

        breaker.success(API);
        assertThat(status().state()).isEqualTo(State.CLOSED);
        assertThatNoException().isThrownBy(() -> breaker.beforeCall(API));
    }

    @Test
    void aFailedTrialOpensAgainAtOnce() {
        fail(PrimCircuitBreaker.FAILURES);
        clock.advance(PrimCircuitBreaker.OPEN_FOR.plusSeconds(1));
        breaker.beforeCall(API);

        breaker.failure(API, "timeout", false);

        assertThat(status().state()).isEqualTo(State.OPEN);
        assertThat(status().openUntil()).isEqualTo(clock.instant().plus(PrimCircuitBreaker.OPEN_FOR));
    }

    @Test
    void aQuotaRefusalOpensAtOnceForLonger() {
        breaker.beforeCall(API);
        breaker.failure(API, "429 Too Many Requests", true);

        assertThat(status().state()).isEqualTo(State.OPEN);
        assertThat(status().openUntil()).isEqualTo(clock.instant().plus(PrimCircuitBreaker.QUOTA_OPEN_FOR));

        clock.advance(PrimCircuitBreaker.OPEN_FOR.plusSeconds(1));
        assertThatThrownBy(() -> breaker.beforeCall(API)).isInstanceOf(PrimUnavailableException.class);
    }

    @Test
    void anAbandonedTrialLetsTheNextCallTry() {
        fail(PrimCircuitBreaker.FAILURES);
        clock.advance(PrimCircuitBreaker.OPEN_FOR.plusSeconds(1));
        breaker.beforeCall(API);

        breaker.abandon(API);

        assertThatNoException().isThrownBy(() -> breaker.beforeCall(API));
    }

    @Test
    void apisAreIndependent() {
        fail(PrimCircuitBreaker.FAILURES);

        assertThatNoException().isThrownBy(() -> breaker.beforeCall(ApiQuota.Api.NAVITIA));
    }

    @Test
    void reportsAnOpenCircuitPastItsDelayAsReadyToTry() {
        fail(PrimCircuitBreaker.FAILURES);
        clock.advance(Duration.ofMinutes(1));

        assertThat(status().state()).isEqualTo(State.TRIAL);
    }

    private void fail(int times) {
        for (int i = 0; i < times; i++) {
            breaker.beforeCall(API);
            breaker.failure(API, "timeout", false);
        }
    }

    private PrimCircuitBreaker.Status status() {
        return breaker.status().stream().filter(status -> status.api() == API).findFirst().orElseThrow();
    }
}
