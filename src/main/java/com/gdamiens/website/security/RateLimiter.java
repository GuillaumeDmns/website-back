package com.gdamiens.website.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * In-memory token buckets, one per key (user, guest device or IP). Buckets unused for a while are dropped: per-minute
 * ones after {@link #IDLE_EVICTION}, longer ones (daily budgets) after their own delay.
 */
@Component
public class RateLimiter {

    private static final Duration IDLE_EVICTION = Duration.ofMinutes(10);

    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    /**
     * Takes one token from the bucket of {@code key}, created with {@code perMinute} capacity if absent.
     */
    public ConsumptionProbe tryConsume(String key, int perMinute) {
        return tryConsume(key, IDLE_EVICTION, () -> perMinuteBucket(perMinute));
    }

    /**
     * Takes one token from the bucket of {@code key}, created by {@code newBucket} if absent and dropped after
     * {@code idleEviction} without use (at least its refill period, or its limits would reset).
     */
    public ConsumptionProbe tryConsume(String key, Duration idleEviction, Supplier<Bucket> newBucket) {
        Entry entry = buckets.computeIfAbsent(key, k -> new Entry(newBucket.get(), idleEviction.toNanos()));
        entry.lastAccessNanos = System.nanoTime();
        return entry.bucket.tryConsumeAndReturnRemaining(1);
    }

    @Scheduled(fixedDelay = 5, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public void evictIdleBuckets() {
        long now = System.nanoTime();
        buckets.values().removeIf(entry -> now - entry.lastAccessNanos > entry.idleEvictionNanos);
    }

    private static Bucket perMinuteBucket(int perMinute) {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(perMinute).refillGreedy(perMinute, Duration.ofMinutes(1)))
            .build();
    }

    private static final class Entry {
        private final Bucket bucket;
        private final long idleEvictionNanos;
        private volatile long lastAccessNanos = System.nanoTime();

        private Entry(Bucket bucket, long idleEvictionNanos) {
            this.bucket = bucket;
            this.idleEvictionNanos = idleEvictionNanos;
        }
    }
}
