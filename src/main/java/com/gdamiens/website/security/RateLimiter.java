package com.gdamiens.website.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token buckets, one per key (user or IP). Buckets unused for a while are dropped.
 */
@Component
public class RateLimiter {

    private static final Duration IDLE_EVICTION = Duration.ofMinutes(10);

    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    /**
     * Takes one token from the bucket of {@code key}, created with {@code perMinute} capacity if absent.
     */
    public ConsumptionProbe tryConsume(String key, int perMinute) {
        Entry entry = buckets.computeIfAbsent(key, k -> new Entry(newBucket(perMinute)));
        entry.lastAccessNanos = System.nanoTime();
        return entry.bucket.tryConsumeAndReturnRemaining(1);
    }

    @Scheduled(fixedDelay = 5, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public void evictIdleBuckets() {
        long threshold = System.nanoTime() - IDLE_EVICTION.toNanos();
        buckets.values().removeIf(entry -> entry.lastAccessNanos < threshold);
    }

    private static Bucket newBucket(int perMinute) {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(perMinute).refillGreedy(perMinute, Duration.ofMinutes(1)))
            .build();
    }

    private static final class Entry {
        private final Bucket bucket;
        private volatile long lastAccessNanos = System.nanoTime();

        private Entry(Bucket bucket) {
            this.bucket = bucket;
        }
    }
}
