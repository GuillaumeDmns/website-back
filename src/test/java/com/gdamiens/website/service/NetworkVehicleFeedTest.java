package com.gdamiens.website.service;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.client.PrimRealtimeClient;
import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.PrimUnavailableException;
import com.gdamiens.website.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NetworkVehicleFeedTest {

    /** 08:00 in Paris */
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-10T06:00:00Z"));

    private final PrimRealtimeClient prim = mock(PrimRealtimeClient.class);

    private final VehiclePacing pacing = new VehiclePacing(new ApiQuota(new ApplicationProperties(), clock), clock);

    private final ApplicationProperties properties = new ApplicationProperties();

    private final NetworkVehicleFeed feed = new NetworkVehicleFeed(prim, pacing, clock, properties);

    private static final byte[] EMPTY = "[]".getBytes(StandardCharsets.UTF_8);

    @Test
    void nothingIsAskedWhileNoLineIsWatched() {
        feed.tick();

        verify(prim, never()).getNetworkVehicleJourneys();
    }

    @Test
    void theNetworkIsReadAboutEveryMinuteWhileALineIsWatched() {
        when(prim.getNetworkVehicleJourneys()).thenReturn(Map.of("C01371", EMPTY));
        pacing.watch("C01742");

        feed.tick();
        clock.advance(Duration.ofSeconds(30));
        feed.tick();

        verify(prim, times(1)).getNetworkVehicleJourneys();
        assertThat(feed.line("C01371")).hasValueSatisfying(line -> assertThat(line.at()).isEqualTo(Instant.parse("2026-10-10T06:00:00Z")));
        assertThat(feed.line("C01742")).isEmpty();

        clock.advance(Duration.ofSeconds(30));
        pacing.watch("C01742");
        feed.tick();

        verify(prim, times(2)).getNetworkVehicleJourneys();
    }

    @Test
    void offOnTheDevelopmentMachine() {
        properties.getVehicles().setNetworkFeed(false);
        NetworkVehicleFeed off = new NetworkVehicleFeed(prim, pacing, clock, properties);
        pacing.watch("C01742");

        off.tick();

        verify(prim, never()).getNetworkVehicleJourneys();
    }

    @Test
    void aFailureKeepsTheSnapshotUntilItIsTooOldAndIsNotTriedAgainAtOnce() {
        when(prim.getNetworkVehicleJourneys()).thenReturn(Map.of("C01371", EMPTY))
            .thenThrow(new PrimUnavailableException("down"));
        pacing.watch("C01371");
        feed.tick();
        clock.advance(Duration.ofSeconds(60));
        pacing.watch("C01371");

        feed.tick();
        clock.advance(Duration.ofSeconds(5));
        feed.tick();

        verify(prim, times(2)).getNetworkVehicleJourneys();
        assertThat(feed.line("C01371")).isPresent();
        clock.advance(Duration.ofSeconds(56));
        assertThat(feed.line("C01371")).isEmpty();
    }
}
