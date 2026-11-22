package com.gdamiens.website.service;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePacingTest {

    /** 08:00 in Paris: a third of the day done */
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-10T06:00:00Z"));

    private final ApiQuota quota = new ApiQuota(new ApplicationProperties(), clock);

    private final VehiclePacing pacing = new VehiclePacing(quota, clock);

    private void used(int requests) {
        for (int i = 0; i < requests; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }
    }

    @Test
    void freshToTheMinuteWhileTheDayIsNotAhead() {
        // 1 350 allowed: 15 % at any time (202) and a third of the rest earned by 08:00 (382)
        used(580);

        assertThat(pacing.interval("C01371")).isEqualTo(Duration.ofSeconds(60));
        assertThat(pacing.interval("C01742")).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void aheadOfTheDayTheLinesWatchedShareWhatItEarns() {
        used(600);

        // A request every 75 s in all (86 400 s for 1 147 requests): one line alone keeps close to its minute
        assertThat(pacing.interval("C01371")).isEqualTo(Duration.ofSeconds(75));
        // Three lines: each every 3 min 46
        pacing.interval("C01742");
        assertThat(pacing.interval("C01743")).isEqualTo(Duration.ofSeconds(226));
        // Never staler than 5 min, however many lines
        for (int line = 0; line < 10; line++) {
            pacing.interval("C0" + line);
        }
        assertThat(pacing.interval("C01371")).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void aLineNobodyAsksAnymoreIsNoLongerCounted() {
        used(600);
        pacing.interval("C01742");
        pacing.interval("C01743");

        clock.advance(Duration.ofMinutes(6));

        assertThat(pacing.interval("C01371")).isEqualTo(Duration.ofSeconds(75));
    }

    @Test
    void theDayCatchesUp() {
        used(600);
        assertThat(pacing.interval("C01371")).isGreaterThan(Duration.ofSeconds(60));

        // An hour later the day has earned 48 more
        clock.advance(Duration.ofHours(1));

        assertThat(pacing.interval("C01371")).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void theNetworkFeedIsOneShareForAllTheLinesItGives() {
        used(600);
        pacing.networkRead(clock.instant(), Set.of("C01371", "C01742", "C01743"));
        pacing.watch("C01371");
        pacing.watch("C01742");

        // The network feed alone: a request every 75 s
        assertThat(pacing.networkInterval()).isEqualTo(Duration.ofSeconds(75));
        // A line it lacks is a second share, for both
        assertThat(pacing.interval("C01100")).isEqualTo(Duration.ofSeconds(151));
        assertThat(pacing.networkInterval()).isEqualTo(Duration.ofSeconds(100));
    }

    @Test
    void theNetworkFeedIsAskedEveryMinuteWhileTheDayIsNotAhead() {
        used(300);
        pacing.networkRead(clock.instant(), Set.of("C01371"));

        assertThat(pacing.networkInterval()).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void aTooOldNetworkSnapshotNoLongerServesItsLines() {
        used(600);
        pacing.networkRead(clock.instant(), Set.of("C01371", "C01742"));
        clock.advance(Duration.ofMinutes(3));
        pacing.watch("C01742");

        // Both lines asked alone again
        assertThat(pacing.interval("C01371")).isEqualTo(Duration.ofSeconds(151));
    }

    @Test
    void linesAreWatchedForFiveMinutes() {
        assertThat(pacing.anyWatched()).isFalse();
        pacing.watch("C01371");
        assertThat(pacing.anyWatched()).isTrue();

        clock.advance(Duration.ofMinutes(6));

        assertThat(pacing.anyWatched()).isFalse();
    }
}
