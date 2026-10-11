package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.QuotaExceededException;
import com.gdamiens.website.security.Role;
import com.gdamiens.website.support.MutableClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiQuotaTest {

    /** 10:15 in Paris */
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-10T08:15:00Z"));

    private ApiQuota quota;

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getQuota().setGuestShare(0.15);
        quota = new ApiQuota(properties, clock);
    }

    @AfterEach
    void clearCaller() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void accountsGetNinetyPercentOfTheDailyQuota() {
        // estimated-timetable: 1 500 a day, 1 350 allowed
        for (int i = 0; i < 1350; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }

        assertThatThrownBy(() -> quota.consume(ApiQuota.Feature.VEHICLES))
            .isInstanceOfSatisfying(QuotaExceededException.class, e -> {
                assertThat(e.getCode()).isEqualTo(QuotaExceededException.QUOTA_EXHAUSTED);
                assertThat(e.isGuestShare()).isFalse();
            });
        assertThat(usage(ApiQuota.Api.ESTIMATED_TIMETABLE).used()).isEqualTo(1350);
    }

    @Test
    void guestsGetTheirShareSpreadOverTheDay() {
        signInAsGuest();
        // 15 % of 1 500 = 225 a day, at most 225 / 16 = 14 an hour
        for (int i = 0; i < 14; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }

        assertThatThrownBy(() -> quota.consume(ApiQuota.Feature.VEHICLES))
            .isInstanceOfSatisfying(QuotaExceededException.class, e -> assertThat(e.isGuestShare()).isTrue());

        // A refused call is not counted
        ApiQuota.Usage usage = usage(ApiQuota.Api.ESTIMATED_TIMETABLE);
        assertThat(usage.used()).isEqualTo(14);
        assertThat(usage.guestUsed()).isEqualTo(14);
        assertThat(usage.guestLimit()).isEqualTo(225);
    }

    @Test
    void guestsGetMoreTheNextHour() {
        signInAsGuest();
        for (int i = 0; i < 14; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }

        clock.advance(Duration.ofHours(1));

        quota.consume(ApiQuota.Feature.VEHICLES);
        assertThat(usage(ApiQuota.Api.ESTIMATED_TIMETABLE).guestUsed()).isEqualTo(15);
    }

    @Test
    void accountsKeepCallingWhenTheGuestsShareIsUsedUp() {
        signInAsGuest();
        for (int i = 0; i < 14; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }
        SecurityContextHolder.clearContext();

        quota.consume(ApiQuota.Feature.VEHICLES);
        assertThat(usage(ApiQuota.Api.ESTIMATED_TIMETABLE).used()).isEqualTo(15);
    }

    @Test
    void countsStartAgainOnAParisDay() {
        for (int i = 0; i < 1350; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }

        // 23:30 in Paris: still the same day
        clock.set(Instant.parse("2026-10-10T21:30:00Z"));
        assertThatThrownBy(() -> quota.consume(ApiQuota.Feature.VEHICLES)).isInstanceOf(QuotaExceededException.class);

        // 00:30 in Paris (22:30 UTC): a new day
        clock.set(Instant.parse("2026-10-10T22:30:00Z"));
        quota.consume(ApiQuota.Feature.VEHICLES);
        assertThat(quota.today()).containsEntry(ApiQuota.Api.ESTIMATED_TIMETABLE, 1);
    }

    @Test
    void apisAreCountedApart() {
        quota.consume(ApiQuota.Feature.JOURNEYS);
        quota.consume(ApiQuota.Feature.JOURNEYS);
        quota.consume(ApiQuota.Feature.DEPARTURES);

        assertThat(quota.today())
            .containsEntry(ApiQuota.Api.NAVITIA, 2)
            .containsEntry(ApiQuota.Api.STOP_MONITORING, 1)
            .containsEntry(ApiQuota.Api.ESTIMATED_TIMETABLE, 0);
    }

    @Test
    void countsPerFeature() {
        quota.consume(ApiQuota.Feature.JOURNEYS);
        quota.consume(ApiQuota.Feature.JOURNEYS);
        signInAsGuest();
        quota.consume(ApiQuota.Feature.PLACES);

        assertThat(quota.featureUsage())
            .contains(new ApiQuota.FeatureUsage(ApiQuota.Feature.JOURNEYS, ApiQuota.Api.NAVITIA, 2, 0))
            .contains(new ApiQuota.FeatureUsage(ApiQuota.Feature.PLACES, ApiQuota.Api.NAVITIA, 1, 1));
        assertThat(quota.today()).containsEntry(ApiQuota.Api.NAVITIA, 3);
    }

    @Test
    void catchesUpWithPrimsOwnCount() {
        // Another user of the key (a development machine) made 1 340 calls today
        quota.consume(ApiQuota.Feature.VEHICLES);
        quota.observe(ApiQuota.Api.ESTIMATED_TIMETABLE, 1500, 160);

        ApiQuota.Usage usage = usage(ApiQuota.Api.ESTIMATED_TIMETABLE);
        assertThat(usage.used()).isEqualTo(1340);
        assertThat(usage.primUsed()).isEqualTo(1340);
        assertThat(usage.primObservedAt()).isEqualTo(clock.instant());
        // 1 350 allowed: ten more, then refused
        for (int i = 0; i < 10; i++) {
            quota.consume(ApiQuota.Feature.VEHICLES);
        }
        assertThatThrownBy(() -> quota.consume(ApiQuota.Feature.VEHICLES)).isInstanceOf(QuotaExceededException.class);

        // An older answer never sends the count back
        quota.observe(ApiQuota.Api.ESTIMATED_TIMETABLE, 1500, 1400);
        assertThat(usage(ApiQuota.Api.ESTIMATED_TIMETABLE).used()).isEqualTo(1350);
    }

    @Test
    void goesOnFromTheCountsSavedBeforeARestart() {
        quota.restore(quota.day(), java.util.List.of(
            new ApiQuota.FeatureUsage(ApiQuota.Feature.JOURNEYS, ApiQuota.Api.NAVITIA, 120, 20),
            new ApiQuota.FeatureUsage(ApiQuota.Feature.TRAFFIC, ApiQuota.Api.NAVITIA, 300, 0)));
        // Another day's counts are ignored
        quota.restore(quota.day().minusDays(1), java.util.List.of(
            new ApiQuota.FeatureUsage(ApiQuota.Feature.VEHICLES, ApiQuota.Api.ESTIMATED_TIMETABLE, 900, 0)));

        quota.consume(ApiQuota.Feature.JOURNEYS);

        assertThat(quota.today()).containsEntry(ApiQuota.Api.NAVITIA, 421).containsEntry(ApiQuota.Api.ESTIMATED_TIMETABLE, 0);
        assertThat(usage(ApiQuota.Api.NAVITIA).guestUsed()).isEqualTo(20);
        assertThat(quota.featureUsage()).contains(new ApiQuota.FeatureUsage(ApiQuota.Feature.JOURNEYS, ApiQuota.Api.NAVITIA, 121, 20));
    }

    private ApiQuota.Usage usage(ApiQuota.Api api) {
        return quota.usage().stream().filter(usage -> usage.api() == api).findFirst().orElseThrow();
    }

    private static void signInAsGuest() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("guest:1", null, Role.ROLE_GUEST.name()));
    }
}
