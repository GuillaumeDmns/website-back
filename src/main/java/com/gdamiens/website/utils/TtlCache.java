package com.gdamiens.website.utils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Small in-memory cache whose entries expire after a fixed duration. Concurrent requests for the same missing key
 * share a single load; failed loads are not cached.
 */
public class TtlCache<K, V> {

    private final Duration ttl;

    private final int maxSize;

    private final Map<K, Entry<V>> entries = new ConcurrentHashMap<>();

    public TtlCache(Duration ttl, int maxSize) {
        this.ttl = ttl;
        this.maxSize = maxSize;
    }

    public V get(K key, Function<K, V> loader) {
        long now = System.nanoTime();
        Entry<V> entry = entries.compute(key, (k, existing) ->
            existing != null && !existing.isExpired(now, ttl) ? existing : new Entry<>(new CompletableFuture<>(), now));

        if (entry.value.isDone() || !entry.claim()) {
            return join(key, entry);
        }

        try {
            entry.value.complete(loader.apply(key));
        } catch (RuntimeException e) {
            entries.remove(key, entry);
            entry.value.completeExceptionally(e);
            throw e;
        }

        if (entries.size() > maxSize) {
            evictExpired();
        }
        return join(key, entry);
    }

    public void invalidateAll() {
        entries.clear();
    }

    private V join(K key, Entry<V> entry) {
        try {
            return entry.value.join();
        } catch (CompletionException e) {
            entries.remove(key, entry);
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }

    private void evictExpired() {
        long now = System.nanoTime();
        entries.values().removeIf(entry -> entry.isExpired(now, ttl));
        if (entries.size() > maxSize) {
            entries.clear();
        }
    }

    private static final class Entry<V> {
        private final CompletableFuture<V> value;
        private final long createdAt;
        private final AtomicBoolean claimed = new AtomicBoolean();

        private Entry(CompletableFuture<V> value, long createdAt) {
            this.value = value;
            this.createdAt = createdAt;
        }

        /** Only the first caller loads the value, the others wait for it */
        private boolean claim() {
            return claimed.compareAndSet(false, true);
        }

        private boolean isExpired(long now, Duration ttl) {
            return value.isDone() && now - createdAt > ttl.toNanos();
        }
    }
}
