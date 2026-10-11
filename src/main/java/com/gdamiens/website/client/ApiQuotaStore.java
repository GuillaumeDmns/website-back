package com.gdamiens.website.client;

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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Keeps {@link ApiQuota}'s counts of the day in {@code public.api_quota_usage}: saved every 30 s and at shutdown, read
 * back at startup, so that a deployment during the day doesn't count from zero again.
 */
@Component
public class ApiQuotaStore {

    private static final Logger log = LoggerFactory.getLogger(ApiQuotaStore.class);

    private final ApiQuota apiQuota;

    private final NamedParameterJdbcTemplate jdbc;

    public ApiQuotaStore(ApiQuota apiQuota, NamedParameterJdbcTemplate jdbc) {
        this.apiQuota = apiQuota;
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void restore() {
        LocalDate day = apiQuota.day();
        List<ApiQuota.FeatureUsage> saved = jdbc.query(
            "SELECT feature, calls, guest_calls FROM public.api_quota_usage WHERE day = :day",
            new MapSqlParameterSource("day", day),
            (rs, i) -> {
                ApiQuota.Feature feature = feature(rs.getString(1));
                return feature == null ? null : new ApiQuota.FeatureUsage(feature, feature.api(), rs.getInt(2), rs.getInt(3));
            }).stream().filter(Objects::nonNull).toList();
        apiQuota.restore(day, saved);
        if (!saved.isEmpty()) {
            log.info("PRIM calls of {} restored: {}", day, apiQuota.today());
        }
    }

    @Scheduled(initialDelay = 30, fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    @PreDestroy
    public void save() {
        LocalDate day = apiQuota.day();
        try {
            for (ApiQuota.FeatureUsage usage : apiQuota.featureUsage()) {
                if (usage.calls() == 0) {
                    continue;
                }
                jdbc.update("""
                        INSERT INTO public.api_quota_usage (day, feature, api, calls, guest_calls) VALUES (:day, :feature, :api, :calls, :guest)
                        ON CONFLICT (day, feature) DO UPDATE SET calls = GREATEST(api_quota_usage.calls, EXCLUDED.calls),
                          guest_calls = GREATEST(api_quota_usage.guest_calls, EXCLUDED.guest_calls), updated_at = now()""",
                    new MapSqlParameterSource().addValue("day", day).addValue("feature", usage.feature().name())
                        .addValue("api", usage.api().name()).addValue("calls", usage.calls()).addValue("guest", usage.guestCalls()));
            }
        } catch (DataAccessException e) {
            log.warn("PRIM call counts not saved: {}", e.getMessage());
        }
    }

    private static ApiQuota.Feature feature(String name) {
        try {
            return ApiQuota.Feature.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
