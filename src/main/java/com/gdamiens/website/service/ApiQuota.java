package com.gdamiens.website.service;

import com.gdamiens.website.exceptions.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Daily quotas of the PRIM APIs (marketplace account, see CLAUDE.md): requests are counted per Paris day and refused
 * above {@link #SAFETY} of the quota, so that the app degrades (no vehicles, schedule only…) instead of having the
 * key blocked. In memory: a restart counts from zero again.
 */
@Component
public class ApiQuota {

    private static final Logger log = LoggerFactory.getLogger(ApiQuota.class);

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    /** Share of a quota used at most, the rest kept for restarts and manual calls */
    private static final double SAFETY = 0.9;

    public enum Api {
        /** Prochains passages, requête unitaire (stop-monitoring) */
        STOP_MONITORING(1_000_000),
        /** Prochains passages, requête globale (estimated-timetable, also with a LineRef) */
        ESTIMATED_TIMETABLE(1_500),
        /** Calculateur Île-de-France Mobilités, accès générique v2 (Navitia: journeys, places, traffic reports…) */
        NAVITIA(20_000);

        final int perDay;

        Api(int perDay) {
            this.perDay = perDay;
        }
    }

    private final Map<Api, AtomicInteger> counts = new EnumMap<>(Api.class);

    private LocalDate day = LocalDate.now(PARIS);

    public ApiQuota() {
        for (Api api : Api.values()) {
            counts.put(api, new AtomicInteger());
        }
    }

    /**
     * Counts a request to [api]
     *
     * @throws CustomException 503 when the day's share is used up
     */
    public void consume(Api api) {
        resetOnNewDay();
        int limit = (int) (api.perDay * SAFETY);
        int count = counts.get(api).incrementAndGet();
        if (count > limit) {
            counts.get(api).decrementAndGet();
            throw new CustomException("Daily quota of " + api + " used up", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (count == limit / 2 || count == limit * 8 / 10) {
            log.warn("{} requests to {} today ({} allowed)", count, api, limit);
        }
    }

    /** Requests counted today, per API */
    public Map<Api, Integer> today() {
        resetOnNewDay();
        Map<Api, Integer> today = new EnumMap<>(Api.class);
        counts.forEach((api, count) -> today.put(api, count.get()));
        return today;
    }

    private synchronized void resetOnNewDay() {
        LocalDate now = LocalDate.now(PARIS);
        if (!now.equals(day)) {
            day = now;
            counts.values().forEach(count -> count.set(0));
        }
    }
}
