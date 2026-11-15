package com.gdamiens.website.service;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.utils.ParisTime;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How often a line's vehicles may be asked to PRIM: its estimated-timetable quota is small (1 500 a day for everyone)
 * and each line watched costs a request a minute. The day's allowance is spread: a reserve usable at any time, the rest
 * earned as the day goes. While the requests made stay under what the day has earned so far, every line is fresh to
 * the minute; once ahead of it, all the lines watched share the rate at which the allowance is earned (about a request
 * every 75 s in all), each one asked less often, never beyond {@link #LONGEST}: vehicles get a little staler at a busy
 * moment instead of disappearing for the rest of the day when the quota is gone. Their expected times stay right
 * meanwhile, and the apps move them along on those.
 */
@Component
public class VehiclePacing {

    /** A line's vehicles are never asked more often */
    static final Duration SHORTEST = Duration.ofSeconds(60);

    /** Nor kept longer: staler, they would mislead (see {@link ApiQuota} for what happens once the quota is used up) */
    static final Duration LONGEST = Duration.ofMinutes(5);

    /** Share of the day's allowance usable at any time, the rest being earned from midnight to midnight */
    static final double RESERVE = 0.15;

    /** A line asked within this long is being watched */
    private static final Duration WATCHED = Duration.ofMinutes(5);

    private static final int SECONDS_PER_DAY = 24 * 3600;

    private final ApiQuota quota;

    private final Clock clock;

    /** When each line was last asked for */
    private final Map<String, Instant> watched = new ConcurrentHashMap<>();

    public VehiclePacing(ApiQuota quota, Clock clock) {
        this.quota = quota;
        this.clock = clock;
    }

    /**
     * @return how old the vehicles of {@code lineId} may be before PRIM is asked again, the line counted as watched
     */
    public Duration interval(String lineId) {
        Instant now = clock.instant();
        watched.put(lineId, now);
        watched.values().removeIf(at -> at.isBefore(now.minus(WATCHED)));

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
        // Ahead of the day: together, the lines watched get what the day earns
        double secondsPerRequest = SECONDS_PER_DAY / ((1 - RESERVE) * usage.limit());
        long seconds = Math.round(watched.size() * secondsPerRequest);
        return Duration.ofSeconds(Math.clamp(seconds, SHORTEST.toSeconds(), LONGEST.toSeconds()));
    }
}
