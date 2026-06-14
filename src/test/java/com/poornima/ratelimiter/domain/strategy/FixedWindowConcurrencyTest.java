package com.poornima.ratelimiter.domain.strategy;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.domain.model.FixedWindowState;
import com.poornima.ratelimiter.infrastructure.storage.InMemoryRateLimitStore;
import com.poornima.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowConcurrencyTest {

        private static final String KEY = "user-123";

        @Test
        void shouldDemonstrateRaceConditionUnderConcurrentLoad()
                        throws InterruptedException {

                int limit = 100;
                int requests = 500;
                int threads = 50;

                InMemoryRateLimitStore store = new InMemoryRateLimitStore();

                MutableClock clock = new MutableClock(
                                Instant.parse("2025-01-01T00:00:00Z"),
                                ZoneOffset.UTC);

                FixedWindowStrategy strategy = new FixedWindowStrategy(store, clock);

                RateLimitConfig config = new RateLimitConfig(
                                limit,
                                Duration.ofMinutes(1));


                ExecutorService executor = Executors.newFixedThreadPool(requests);

                CountDownLatch ready = new CountDownLatch(requests);
                CountDownLatch start = new CountDownLatch(1);
                CountDownLatch done = new CountDownLatch(requests);

                AtomicInteger allowed = new AtomicInteger();
                AtomicInteger blocked = new AtomicInteger();

                for (int i = 0; i < requests; i++) {

                        executor.submit(() -> {
                                try {
                                        ready.countDown();

                                        start.await();

                                        RateLimitResult result = strategy.evaluate(KEY, config);

                                        if (result.allowed()) {
                                                allowed.incrementAndGet();
                                        } else {
                                                blocked.incrementAndGet();
                                        }

                                } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                } finally {
                                        done.countDown();
                                }
                        });
                }

                ready.await();

                long startTime = System.currentTimeMillis();

                start.countDown();

                done.await();

                long endTime = System.currentTimeMillis();

                executor.shutdown();

                FixedWindowState state = (FixedWindowState) store.get(KEY);

                System.out.println("--------------------------------");
                System.out.println("Execution Time = "
                                + (endTime - startTime) + " ms");
                System.out.println("Allowed = " + allowed.get());
                System.out.println("Blocked = " + blocked.get());
                System.out.println("Stored Count = "
                                + (state != null ? state.count() : 0));
                System.out.println("--------------------------------");

                assertTrue(
                                allowed.get() >= limit,
                                "Allowed requests should be at least the configured limit");
        }
}