package com.gdamiens.website.service;

import com.gdamiens.website.client.PrimRealtimeClient;
import com.gdamiens.website.client.siri.EstimatedVehicleJourney;
import com.gdamiens.website.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The vehicle journeys of the whole network, asked to PRIM in one request (estimated-timetable "requête globale") about
 * every minute while some line's vehicles are watched, nothing while nobody watches any ({@link VehiclePacing} gives
 * the pace). One request serves every line, where asking each line alone costs a request per line: the
 * estimated-timetable quota is small. A line missing from it or a snapshot older than
 * {@link #MAX_AGE} (PRIM down, quota used up) are asked line by line, as before ({@link VehicleService}).
 * <p>
 * Off with {@code application.vehicles.network-feed} (the development machine, which shares the production's PRIM
 * key). The snapshot lives in memory, each line's journeys kept as JSON until asked for (about 30 MB in all).
 */
@Component
public class NetworkVehicleFeed {

    private static final Logger log = LoggerFactory.getLogger(NetworkVehicleFeed.class);

    /** An older snapshot is not used: its lines are asked alone */
    public static final Duration MAX_AGE = Duration.ofMinutes(2);

    /**
     * One line's journeys in a snapshot
     *
     * @param at when the snapshot was read
     */
    public record Line(Instant at, byte[] json) {

        public List<EstimatedVehicleJourney> journeys() {
            return PrimRealtimeClient.readJourneys(json);
        }
    }

    private record Snapshot(Instant at, Map<String, byte[]> lines) {
    }

    private final PrimRealtimeClient primRealtimeClient;

    private final VehiclePacing pacing;

    private final Clock clock;

    private final boolean enabled;

    private final ReentrantLock reading = new ReentrantLock();

    private volatile Snapshot snapshot;

    /** Last request, answered or not: a failure is not tried again before the next one is due */
    private volatile Instant attempted;

    public NetworkVehicleFeed(PrimRealtimeClient primRealtimeClient, VehiclePacing pacing, Clock clock,
                              ApplicationProperties applicationProperties) {
        this.primRealtimeClient = primRealtimeClient;
        this.pacing = pacing;
        this.clock = clock;
        this.enabled = applicationProperties.getVehicles().isNetworkFeed();
    }

    /**
     * @return the line's journeys in the current snapshot, empty when it is too old or lacks the line (then the line
     * is asked alone)
     */
    public Optional<Line> line(String lineId) {
        Snapshot current = snapshot;
        if (current == null || current.at().isBefore(clock.instant().minus(MAX_AGE))) {
            return Optional.empty();
        }
        return Optional.ofNullable(current.lines().get(lineId)).map(json -> new Line(current.at(), json));
    }

    /** Reads the network when lines are watched and the snapshot is due */
    @Scheduled(initialDelay = 5, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void tick() {
        if (!enabled || !pacing.anyWatched()) {
            return;
        }
        Instant last = attempted;
        if (last != null && last.isAfter(clock.instant().minus(pacing.networkInterval()))) {
            return;
        }
        read();
    }

    /** Asks PRIM for the whole network now; the current snapshot is kept when it fails */
    public void read() {
        if (!reading.tryLock()) {
            return;
        }
        try {
            Instant start = clock.instant();
            attempted = start;
            Map<String, byte[]> lines = primRealtimeClient.getNetworkVehicleJourneys();
            Instant at = clock.instant();
            snapshot = new Snapshot(at, Map.copyOf(lines));
            pacing.networkRead(at, lines.keySet());
            log.info("Network vehicle journeys read in {} ms: {} lines", Duration.between(start, at).toMillis(), lines.size());
        } catch (RuntimeException e) {
            // Quota used up, PRIM down or an unreadable answer: lines are asked alone once the snapshot is too old
            log.warn("Network vehicle journeys not read: {}", e.getMessage());
        } finally {
            reading.unlock();
        }
    }

    /** Forgets the snapshot (integration tests: one application for all of them) */
    public void clear() {
        snapshot = null;
        attempted = null;
        pacing.networkRead(Instant.EPOCH, Set.of());
    }
}
