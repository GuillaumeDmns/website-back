package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.QuotaExceededException;
import com.gdamiens.website.security.Role;
import com.gdamiens.website.utils.ParisTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * guests there are, the accounts keep the rest. Background tasks count as accounts.
 * <p>
 * Each request is counted under the {@link Feature} that made it, to know where the quotas go. The count follows
 * PRIM's own (its answers say how much of the day's quota is left: other users of the key, such as a development
 * machine, count too) and survives restarts ({@code ApiQuotaStore} saves it).
 */
@Component
public class ApiQuota {

    private static final Logger log = LoggerFactory.getLogger(ApiQuota.class);

    /** Share of a quota used at most, the rest kept for restarts and manual calls */
    private static final double SAFETY = 0.9;

    /** The guests' share is spread over this many hours at least, so that a morning burst can't use the whole day's */
    private static final int GUEST_HOURS = 16;

    public enum Api {
        /** Prochains passages, requête unitaire (stop-monitoring) */
        STOP_MONITORING(1_000_000),
        /** Prochains passages, requête globale (estimated-timetable, also with a LineRef: measured 11 October 2026) */
        ESTIMATED_TIMETABLE(1_500),
        /** Calculateur Île-de-France Mobilités, accès générique v2 (Navitia: journeys, places, walking paths) */
        NAVITIA(20_000),
        /** Messages Info Trafic, requête globale (disruptions_bulk) */
        DISRUPTIONS_BULK(18_000);

        final int perDay;

        Api(int perDay) {
            this.perDay = perDay;
        }

        /** PRIM's daily quota */
        public int perDay() {
            return perDay;
        }
    }

    /** What a request is for: where the quotas go */
    public enum Feature {
        DEPARTURES(Api.STOP_MONITORING),
        VEHICLES(Api.ESTIMATED_TIMETABLE),
        JOURNEYS(Api.NAVITIA),
        /** Walking paths of the Vélib rides */
        WALKING_PATHS(Api.NAVITIA),
        PLACES(Api.NAVITIA),
        TRAFFIC(Api.DISRUPTIONS_BULK);

        private final Api api;

        Feature(Api api) {
            this.api = api;
        }

        public Api api() {
            return api;
        }
    }

    private record Counters(AtomicInteger day, AtomicInteger guestDay, AtomicInteger guestHour) {
        Counters() {
            this(new AtomicInteger(), new AtomicInteger(), new AtomicInteger());
        }
    }

    /** What PRIM said at its last answer */
    private record Observed(int used, int limit, Instant at) {
    }

    private final Map<Api, Counters> counters = new EnumMap<>(Api.class);

    private final Map<Feature, Counters> features = new EnumMap<>(Feature.class);

    private final Map<Api, Observed> observed = new EnumMap<>(Api.class);

    private final double guestShare;

    private final Clock clock;

    private LocalDateTime hour;

    public ApiQuota(ApplicationProperties applicationProperties, Clock clock) {
        this.guestShare = applicationProperties.getQuota().getGuestShare();
        this.clock = clock;
        this.hour = currentHour();
        for (Api api : Api.values()) {
            counters.put(api, new Counters());
        }
        for (Feature feature : Feature.values()) {
            features.put(feature, new Counters());
        }
    }

    /**
     * Counts a request for [feature] by the current caller
     *
     * @throws QuotaExceededException 503 when the day's quota, or the guests' share for a guest, is used up
     */
    public void consume(Feature feature) {
        resetOnNewPeriod();
        Api api = feature.api();
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
        features.get(feature).day().incrementAndGet();
        if (guest) {
            features.get(feature).guestDay().incrementAndGet();
        }
        if (count == limit / 2 || count == limit * 8 / 10) {
            log.warn("{} requests to {} today ({} allowed, {} by guests)", count, api, limit, counter.guestDay().get());
        }
    }

    /**
     * PRIM's count, from the headers of its answer: the local count catches up with it (the key's other users count
     * too), never goes back
     *
     * @param limit     {@code x-ratelimit-limit-day}
     * @param remaining {@code x-ratelimit-remaining-day}
     */
    public void observe(Api api, int limit, int remaining) {
        resetOnNewPeriod();
        int used = Math.max(0, limit - remaining);
        observed.put(api, new Observed(used, limit, clock.instant()));
        counters.get(api).day().accumulateAndGet(used, Math::max);
        if (limit != api.perDay) {
            log.warn("PRIM's daily quota of {} is {}, not {}", api, limit, api.perDay);
        }
    }

    /**
     * Use of a quota today, for the admin status
     *
     * @param primUsed       used according to PRIM at its last answer (null before one today)
     * @param primObservedAt when PRIM said so
     */
    public record Usage(Api api, int used, int limit, int guestUsed, int guestLimit, Integer primUsed, Integer primLimit,
                        Instant primObservedAt) {
    }

    public List<Usage> usage() {
        resetOnNewPeriod();
        List<Usage> usage = new ArrayList<>();
        counters.forEach((api, counter) -> {
            Observed prim = observed.get(api);
            usage.add(new Usage(api, counter.day().get(), (int) (api.perDay * SAFETY), counter.guestDay().get(),
                (int) (api.perDay * guestShare), prim == null ? null : prim.used(), prim == null ? null : prim.limit(),
                prim == null ? null : prim.at()));
        });
        return usage;
    }

    /** Requests of a feature today, by this server, and the guests' share of them */
    public record FeatureUsage(Feature feature, Api api, int calls, int guestCalls) {
    }

    public List<FeatureUsage> featureUsage() {
        resetOnNewPeriod();
        List<FeatureUsage> usage = new ArrayList<>();
        features.forEach((feature, counter) ->
            usage.add(new FeatureUsage(feature, feature.api(), counter.day().get(), counter.guestDay().get())));
        return usage;
    }

    /** Requests counted today, per API */
    public Map<Api, Integer> today() {
        resetOnNewPeriod();
        Map<Api, Integer> today = new EnumMap<>(Api.class);
        counters.forEach((api, counter) -> today.put(api, counter.day().get()));
        return today;
    }

    /** The Paris day being counted */
    public LocalDate day() {
        resetOnNewPeriod();
        return hour.toLocalDate();
    }

    /**
     * Counts of a day saved before a restart: kept when it is today (the counts go on from there)
     */
    public void restore(LocalDate day, List<FeatureUsage> saved) {
        if (!day.equals(day())) {
            return;
        }
        for (FeatureUsage usage : saved) {
            features.get(usage.feature()).day().accumulateAndGet(usage.calls(), Math::max);
            features.get(usage.feature()).guestDay().accumulateAndGet(usage.guestCalls(), Math::max);
        }
        for (Api api : Api.values()) {
            int calls = saved.stream().filter(usage -> usage.api() == api).mapToInt(FeatureUsage::calls).sum();
            int guestCalls = saved.stream().filter(usage -> usage.api() == api).mapToInt(FeatureUsage::guestCalls).sum();
            counters.get(api).day().accumulateAndGet(calls, Math::max);
            counters.get(api).guestDay().accumulateAndGet(guestCalls, Math::max);
        }
    }

    private synchronized void resetOnNewPeriod() {
        LocalDateTime now = currentHour();
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
        if (newDay) {
            features.values().forEach(counter -> {
                counter.day().set(0);
                counter.guestDay().set(0);
            });
            observed.clear();
        }
    }

    private LocalDateTime currentHour() {
        return LocalDateTime.now(clock.withZone(ParisTime.ZONE)).truncatedTo(ChronoUnit.HOURS);
    }
}
