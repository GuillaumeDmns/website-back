package com.gdamiens.website.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TtlCacheTest {

    @Test
    void loadsOnceWhileFresh() {
        TtlCache<String, Integer> cache = new TtlCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();

        assertThat(cache.get("a", key -> loads.incrementAndGet())).isEqualTo(1);
        assertThat(cache.get("a", key -> loads.incrementAndGet())).isEqualTo(1);
        assertThat(cache.get("b", key -> loads.incrementAndGet())).isEqualTo(2);
        assertThat(loads).hasValue(2);
    }

    @Test
    void reloadsOnceExpired() throws InterruptedException {
        TtlCache<String, Integer> cache = new TtlCache<>(Duration.ofMillis(20), 10);
        AtomicInteger loads = new AtomicInteger();

        cache.get("a", key -> loads.incrementAndGet());
        Thread.sleep(40);

        assertThat(cache.get("a", key -> loads.incrementAndGet())).isEqualTo(2);
    }

    @Test
    void doesNotCacheFailures() {
        TtlCache<String, Integer> cache = new TtlCache<>(Duration.ofMinutes(1), 10);

        assertThatThrownBy(() -> cache.get("a", key -> {
            throw new IllegalStateException("down");
        })).isInstanceOf(IllegalStateException.class).hasMessage("down");

        assertThat(cache.get("a", key -> 42)).isEqualTo(42);
    }

    @Test
    void invalidateAllForgetsEverything() {
        TtlCache<String, Integer> cache = new TtlCache<>(Duration.ofMinutes(1), 10);
        cache.get("a", key -> 1);

        cache.invalidateAll();

        assertThat(cache.get("a", key -> 2)).isEqualTo(2);
    }

    @Test
    void concurrentCallersShareOneLoad() throws Exception {
        TtlCache<String, Integer> cache = new TtlCache<>(Duration.ofMinutes(1), 10);
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch loading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Integer> first = executor.submit(() -> cache.get("a", key -> {
                loading.countDown();
                await(release);
                return loads.incrementAndGet();
            }));
            assertThat(loading.await(5, TimeUnit.SECONDS)).isTrue();

            List<Future<Integer>> others = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                others.add(executor.submit(() -> cache.get("a", key -> loads.incrementAndGet())));
            }
            release.countDown();

            assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(1);
            for (Future<Integer> other : others) {
                assertThat(other.get(5, TimeUnit.SECONDS)).isEqualTo(1);
            }
        }
        assertThat(loads).hasValue(1);
    }

    @Test
    void staysBoundedWhenFull() {
        TtlCache<Integer, Integer> cache = new TtlCache<>(Duration.ofMinutes(1), 3);
        AtomicInteger loads = new AtomicInteger();

        for (int i = 0; i < 4; i++) {
            cache.get(i, key -> loads.incrementAndGet());
        }

        // Above its size the cache was emptied: the first key is loaded again
        cache.get(0, key -> loads.incrementAndGet());
        assertThat(loads).hasValue(5);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
