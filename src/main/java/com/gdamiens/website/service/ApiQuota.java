package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.QuotaExceededException;
import com.gdamiens.website.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Daily quotas of the PRIM APIs (marketplace account, see CLAUDE.md): requests are counted per Paris day and refused
 * above {@link #SAFETY} of the quota, so that the app degrades (no vehicles, schedule only…) instead of having the
 * key blocked. Guest devices (the caller's authentication, propagated to the worker threads) only get
 * {@code application.quota.guest-share} of each quota, at most {@link #GUEST_HOURS}th of it per hour: however many
 * guests there are, the accounts keep the rest. Background tasks count as accounts. In memory: a restart counts from
 * zero again.
 */
@Component
public class ApiQuota {

    private static final Logger log = LoggerFactory.getLogger(ApiQuota.class);

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    /** Share of a quota used at most, the rest kept for restarts and manual calls */
    private static final double SAFETY = 0.9;

    /** The guests' share is spread over this many hours at least, so that a morning burst can't use the whole day's */
    private static final int GUEST_HOURS = 16;

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

    private record Counters(AtomicInteger day, AtomicInteger guestDay, AtomicInteger guestHour) {
        Counters() {
            this(new AtomicInteger(), new AtomicInteger(), new AtomicInteger());
        }
    }

    private final Map<Api, Counters> counters = new EnumMap<>(Api.class);

    private final double guestShare;

    private LocalDateTime hour = LocalDateTime.now(PARIS).truncatedTo(ChronoUnit.HOURS);

    public ApiQuota(ApplicationProperties applicationProperties) {
        this.guestShare = applicationProperties.getQuota().getGuestShare();
        for (Api api : Api.values()) {
            counters.put(api, new Counters());
        }
    }

    /**
     * Counts a request to [api] for the current caller
     *
     * @throws QuotaExceededException 503 when the day's quota, or the guests' share for a guest, is used up
     */
    public void consume(Api api) {
        resetOnNewPeriod();
        Counters counter = counters.get(api);

        boolean guest = Role.isGuest(SecurityContextHolder.getContext().getAuthentication());
        if (guest) {
            int guestLimit = (int) (api.perDay * guestShare);
            int guestHourLimit = Math.max(1, guestLimit / GUEST_HOURS);
            if (counter.guestDay().incrementAndGet() > guestLimit | counter.guestHour().incrementAndGet() > guestHourLimit) {
                counter.guestDay().decrementAndGet();
                counter.guestHour().decrementAndGet();
                throw new QuotaExceededException("Guest share of " + api + " used up", QuotaExceededException.GUEST_QUOTA_EXHAUSTED);
            }
        }

        int limit = (int) (api.perDay * SAFETY);
        int count = counter.day().incrementAndGet();
        if (count > limit) {
            counter.day().decrementAndGet();
            if (guest) {
                counter.guestDay().decrementAndGet();
                counter.guestHour().decrementAndGet();
            }
            throw new QuotaExceededException("Daily quota of " + api + " used up", QuotaExceededException.QUOTA_EXHAUSTED);
        }
        if (count == limit / 2 || count == limit * 8 / 10) {
            log.warn("{} requests to {} today ({} allowed, {} by guests)", count, api, limit, counter.guestDay().get());
        }
    }

    /** Use of a quota today, for the admin status */
    public record Usage(Api api, int used, int limit, int guestUsed, int guestLimit) {
    }

    public List<Usage> usage() {
        resetOnNewPeriod();
        List<Usage> usage = new ArrayList<>();
        counters.forEach((api, counter) -> usage.add(new Usage(api, counter.day().get(), (int) (api.perDay * SAFETY),
            counter.guestDay().get(), (int) (api.perDay * guestShare))));
        return usage;
    }

    /** Requests counted today, per API */
    public Map<Api, Integer> today() {
        resetOnNewPeriod();
        Map<Api, Integer> today = new EnumMap<>(Api.class);
        counters.forEach((api, counter) -> today.put(api, counter.day().get()));
        return today;
    }

    private synchronized void resetOnNewPeriod() {
        LocalDateTime now = LocalDateTime.now(PARIS).truncatedTo(ChronoUnit.HOURS);
        if (now.equals(hour)) {
            return;
        }
        boolean newDay = !now.toLocalDate().equals(hour.toLocalDate());
        hour = now;
        counters.values().forEach(counter -> {
            counter.guestHour().set(0);
            if (newDay) {
                counter.day().set(0);
                counter.guestDay().set(0);
            }
        });
    }
}
