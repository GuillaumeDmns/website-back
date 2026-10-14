package com.gdamiens.website.usage;

import com.gdamiens.website.utils.ParisTime;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Anonymous usage counts for the administration, kept in memory and added to the database every minute (and at
 * shutdown):
 * <ul>
 *     <li>API requests per hour, route, kind of caller and status, with their time ({@code usage_hourly});</li>
 *     <li>distinct members of a metric per Paris day (active accounts and guest devices, devices per platform and
 *     version, visitors of a site): keyed hashes with the day's random salt ({@code usage_member}); once the day is
 *     over they become a count ({@code usage_daily}) and the hashes and the salt are deleted, so that nothing links a
 *     count to anyone any more.</li>
 * </ul>
 * No account id, IP or position is stored.
 */
@Component
public class UsageRecorder {

    private static final Logger log = LoggerFactory.getLogger(UsageRecorder.class);

    /** Hourly counts kept this long */
    private static final int HOURLY_DAYS = 400;

    private static final int MEMBER_BYTES = 8;

    public enum Caller { ACCOUNT, GUEST, ANONYMOUS }

    record HourKey(Instant hour, String endpoint, Caller caller, String status) {
    }

    /** Requests of an hour key; changed only inside the map's compute */
    static final class Counter {
        long requests;
        long totalMillis;
        long maxMillis;

        void add(long requests, long totalMillis, long maxMillis) {
            this.requests += requests;
            this.totalMillis += totalMillis;
            this.maxMillis = Math.max(this.maxMillis, maxMillis);
        }
    }

    record MemberKey(LocalDate day, String metric) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    private final TransactionTemplate transaction;

    private final Clock clock;

    private final SecureRandom random = new SecureRandom();

    private final ConcurrentHashMap<HourKey, Counter> hours = new ConcurrentHashMap<>();

    /** Members seen today (hex hashes), so that each is saved once */
    private final ConcurrentHashMap<MemberKey, Set<String>> seen = new ConcurrentHashMap<>();

    /** Members not saved yet */
    private final ConcurrentHashMap<MemberKey, Set<String>> pending = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<LocalDate, byte[]> salts = new ConcurrentHashMap<>();

    public UsageRecorder(NamedParameterJdbcTemplate jdbc, TransactionTemplate transaction, Clock clock) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.clock = clock;
    }

    /** An API request, after its answer */
    public void request(String endpoint, Caller caller, int status, long millis) {
        HourKey key = new HourKey(clock.instant().truncatedTo(ChronoUnit.HOURS), endpoint, caller, statusClass(status));
        hours.compute(key, (k, counter) -> {
            Counter current = counter == null ? new Counter() : counter;
            current.add(1, millis, millis);
            return current;
        });
    }

    /** [identity] (an account, a device, a visitor) counted once in [metric] today, as a keyed hash */
    public void member(String metric, String identity) {
        member(metric, identity, clock.instant());
    }

    /** The same at [at] (a page view read from the log a few minutes later) */
    public void member(String metric, String identity, Instant at) {
        LocalDate day = LocalDate.ofInstant(at, ParisTime.ZONE);
        if (day.isBefore(today().minusDays(1))) {
            return; // Its day is closed
        }
        String hash = HexFormat.of().formatHex(hash(salt(day), identity));
        MemberKey key = new MemberKey(day, metric);
        if (seen.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(hash)) {
            pending.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(hash);
        }
    }

    /** 2xx, 3xx, 4xx, 5xx, and 429 (rate limited) apart */
    static String statusClass(int status) {
        return status == 429 ? "429" : (status / 100) + "xx";
    }

    @Scheduled(initialDelay = 60, fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    @PreDestroy
    public synchronized void flush() {
        for (HourKey key : List.copyOf(hours.keySet())) {
            Counter counter = hours.remove(key);
            if (counter == null) {
                continue;
            }
            try {
                jdbc.update("""
                        INSERT INTO public.usage_hourly (hour, endpoint, caller, status, requests, total_millis, max_millis)
                        VALUES (:hour, :endpoint, :caller, :status, :requests, :total, :max)
                        ON CONFLICT (hour, endpoint, caller, status) DO UPDATE SET requests = usage_hourly.requests + EXCLUDED.requests,
                          total_millis = usage_hourly.total_millis + EXCLUDED.total_millis,
                          max_millis = GREATEST(usage_hourly.max_millis, EXCLUDED.max_millis)""",
                    new MapSqlParameterSource().addValue("hour", java.sql.Timestamp.from(key.hour())).addValue("endpoint", key.endpoint())
                        .addValue("caller", key.caller().name()).addValue("status", key.status()).addValue("requests", counter.requests)
                        .addValue("total", counter.totalMillis).addValue("max", counter.maxMillis));
            } catch (DataAccessException e) {
                // Counted again at the next flush
                hours.merge(key, counter, (current, failed) -> {
                    current.add(failed.requests, failed.totalMillis, failed.maxMillis);
                    return current;
                });
                log.warn("Usage counts not saved: {}", e.getMessage());
                return;
            }
        }
        for (MemberKey key : List.copyOf(pending.keySet())) {
            Set<String> members = pending.remove(key);
            if (members == null || members.isEmpty()) {
                continue;
            }
            List<MapSqlParameterSource> rows = new ArrayList<>();
            for (String member : members) {
                rows.add(new MapSqlParameterSource().addValue("day", key.day()).addValue("metric", key.metric())
                    .addValue("member", HexFormat.of().parseHex(member)));
            }
            try {
                jdbc.batchUpdate("INSERT INTO public.usage_member (day, metric, member) VALUES (:day, :metric, :member) ON CONFLICT DO NOTHING",
                    rows.toArray(MapSqlParameterSource[]::new));
            } catch (DataAccessException e) {
                pending.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).addAll(members);
                log.warn("Usage members not saved: {}", e.getMessage());
                return;
            }
        }
    }

    /**
     * Turns the members of the days before today into counts and forgets them with their salt; drops the hourly counts
     * older than {@link #HOURLY_DAYS} days. Every night, and at startup (the server may have been down at night).
     */
    @Scheduled(cron = "0 10 0 * * *", zone = "Europe/Paris")
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void closeDays() {
        flush();
        LocalDate today = today();
        try {
            transaction.executeWithoutResult(status -> {
                MapSqlParameterSource params = new MapSqlParameterSource("today", today);
                jdbc.update("""
                    INSERT INTO public.usage_daily (day, metric, count)
                    SELECT day, metric, count(*) FROM public.usage_member WHERE day < :today GROUP BY day, metric
                    ON CONFLICT (day, metric) DO UPDATE SET count = usage_daily.count + EXCLUDED.count""", params);
                jdbc.update("DELETE FROM public.usage_member WHERE day < :today", params);
                jdbc.update("DELETE FROM public.usage_salt WHERE day < :today", params);
                jdbc.update("DELETE FROM public.usage_hourly WHERE hour < now() - make_interval(days => :days)",
                    new MapSqlParameterSource("days", HOURLY_DAYS));
            });
        } catch (DataAccessException e) {
            log.warn("Usage days not closed: {}", e.getMessage());
            return;
        }
        salts.keySet().removeIf(day -> day.isBefore(today));
        seen.keySet().removeIf(key -> key.day().isBefore(today));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(ParisTime.ZONE));
    }

    /** The day's random salt, shared by every instance and restart of the day through the database */
    private byte[] salt(LocalDate day) {
        return salts.computeIfAbsent(day, d -> {
            byte[] salt = new byte[32];
            random.nextBytes(salt);
            MapSqlParameterSource params = new MapSqlParameterSource("day", d).addValue("salt", salt);
            jdbc.update("INSERT INTO public.usage_salt (day, salt) VALUES (:day, :salt) ON CONFLICT DO NOTHING", params);
            return jdbc.queryForObject("SELECT salt FROM public.usage_salt WHERE day = :day", params, byte[].class);
        });
    }

    static byte[] hash(byte[] salt, String identity) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(salt, "HmacSHA256"));
            return Arrays.copyOf(mac.doFinal(identity.getBytes(StandardCharsets.UTF_8)), MEMBER_BYTES);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
