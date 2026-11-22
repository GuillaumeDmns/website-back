package com.gdamiens.website.service;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.utils.ParisTime;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How often vehicles may be asked to PRIM: its estimated-timetable quota is small (1 500 a day for everyone). They come
 * from the whole network's feed ({@link NetworkVehicleFeed}, a request a minute while lines are watched) and, for the
 * lines it lacks or while it is down, from a request per line a minute. The day's allowance is spread: a
 * reserve usable at any time, the rest earned as the day goes. While the requests made stay under what the day has
 * earned so far, everything is fresh to the minute; once ahead of it, the network feed and the lines asked alone share
 * the rate at which the allowance is earned (about a request every 75 s in all), each asked less often: the network
 * feed at most every {@link #NETWORK_LONGEST} (so that it never gets too old to be used), a line alone at most every
 * {@link #LONGEST}. Vehicles get a little staler at a busy moment instead of disappearing for the rest of the day when
 * the quota is gone. Their expected times stay right meanwhile, and the apps move them along on those.
 */
@Component
public class VehiclePacing {

    /** A line's vehicles, or the network's, are never asked more often */
    static final Duration SHORTEST = Duration.ofSeconds(60);

    /** Nor kept longer: staler, they would mislead (see {@link ApiQuota} for what happens once the quota is used up) */
    static final Duration LONGEST = Duration.ofMinutes(5);

    /** The network feed is asked at least this often while lines are watched: under {@link NetworkVehicleFeed#MAX_AGE} */
    static final Duration NETWORK_LONGEST = Duration.ofSeconds(100);

    /** Share of the day's allowance usable at any time, the rest being earned from midnight to midnight */
    static final double RESERVE = 0.15;

    /** A line asked within this long is being watched */
    private static final Duration WATCHED = Duration.ofMinutes(5);

    private static final int SECONDS_PER_DAY = 24 * 3600;

    private final ApiQuota quota;

    private final Clock clock;

    /** When each line was last asked for */
    private final Map<String, Instant> watched = new ConcurrentHashMap<>();

    /** The lines of the network feed's last snapshot, and when it was taken */
    private volatile Network network;

    private record Network(Instant at, Set<String> lineIds) {
    }

    public VehiclePacing(ApiQuota quota, Clock clock) {
        this.quota = quota;
        this.clock = clock;
    }

    /** {@code lineId}'s vehicles are asked for: the line is watched for a while */
    public void watch(String lineId) {
        Instant now = clock.instant();
        watched.put(lineId, now);
        watched.values().removeIf(at -> at.isBefore(now.minus(WATCHED)));
    }

    /** Some line is being watched: the network feed is worth asking */
    public boolean anyWatched() {
        Instant since = clock.instant().minus(WATCHED);
        return watched.values().stream().anyMatch(at -> at.isAfter(since));
    }

    /** The network feed has just been read: its lines no longer need asking alone */
    public void networkRead(Instant at, Set<String> lineIds) {
        network = new Network(at, Set.copyOf(lineIds));
    }

    /**
     * @return how old the vehicles of {@code lineId}, asked alone, may be before PRIM is asked again, the line counted
     * as watched
     */
    public Duration interval(String lineId) {
        watch(lineId);
        return interval(LONGEST);
    }

    /** @return how old the network feed may be before PRIM is asked again */
    public Duration networkInterval() {
        return interval(NETWORK_LONGEST);
    }

    private Duration interval(Duration longest) {
        Instant now = clock.instant();
        ApiQuota.Usage usage = quota.usage().stream()
            .filter(api -> api.api() == ApiQuota.Api.ESTIMATED_TIMETABLE)
            .findFirst()
            .orElseThrow();
        ZonedDateTime paris = now.atZone(ParisTime.ZONE);
        double dayDone = (double) paris.toLocalTime().toSecondOfDay() / SECONDS_PER_DAY;
        double earned = usage.limit() * (RESERVE + (1 - RESERVE) * dayDone);
        if (usage.used() <= earned) {
            return SHORTEST;
        }
        // Ahead of the day: together, the network feed and the lines asked alone get what the day earns
        double secondsPerRequest = SECONDS_PER_DAY / ((1 - RESERVE) * usage.limit());
        long seconds = Math.round(Math.max(1, shares(now)) * secondsPerRequest);
        return Duration.ofSeconds(Math.clamp(seconds, SHORTEST.toSeconds(), longest.toSeconds()));
    }

    /** The network feed while it is in use, and each line watched that it doesn't give */
    private int shares(Instant now) {
        Network current = network;
        boolean networkUsed = current != null && !current.at().isBefore(now.minus(NetworkVehicleFeed.MAX_AGE));
        Instant since = now.minus(WATCHED);
        long alone = watched.entrySet().stream()
            .filter(entry -> entry.getValue().isAfter(since))
            .filter(entry -> !networkUsed || !current.lineIds().contains(entry.getKey()))
            .count();
        return (int) alone + (networkUsed ? 1 : 0);
    }
}
