package com.poornima.ratelimiter.benchmark;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.strategy.FixedWindowStrategy;
import com.poornima.ratelimiter.domain.strategy.SlidingWindowCounterStrategy;
import com.poornima.ratelimiter.infrastructure.storage.InMemoryRateLimitStore;
import com.poornima.ratelimiter.infrastructure.storage.InMemorySlidingWindowStore;
import com.poornima.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

class RateLimiterBenchmark {

    private static final long REQUESTS = 1_000_000;

    @Test
    void benchmarkFixedWindow() {

        MutableClock clock = new MutableClock(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneOffset.UTC);

        FixedWindowStrategy strategy = new FixedWindowStrategy(
                new InMemoryRateLimitStore(),
                clock);

        RateLimitConfig config = new RateLimitConfig(
                Long.MAX_VALUE,
                Duration.ofMinutes(1));

        BenchmarkResult result = benchmark("Fixed Window", strategy, config);

        print(result);
    }

    @Test
    void benchmarkSlidingWindow() {

        MutableClock clock = new MutableClock(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneOffset.UTC);

        SlidingWindowCounterStrategy strategy = new SlidingWindowCounterStrategy(
                new InMemorySlidingWindowStore(),
                clock);

        RateLimitConfig config = new RateLimitConfig(
                Long.MAX_VALUE,
                Duration.ofMinutes(1));

        BenchmarkResult result = benchmark("Sliding Window", strategy, config);

        print(result);
    }

    @Test
    void compareBoundaryBurstBehavior() {

        MutableClock clock = new MutableClock(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneOffset.UTC);

        FixedWindowStrategy fixed = new FixedWindowStrategy(
                new InMemoryRateLimitStore(),
                clock);

        RateLimitConfig config = new RateLimitConfig(
                100,
                Duration.ofMinutes(1));

        int allowed = 0;

        for (int i = 0; i < 100; i++) {
            if (fixed.evaluate("user", config).allowed()) {
                allowed++;
            }
        }

        clock.advance(Duration.ofSeconds(59));

        for (int i = 0; i < 100; i++) {
            if (fixed.evaluate("user", config).allowed()) {
                allowed++;
            }
        }

        clock.advance(Duration.ofSeconds(1));

        for (int i = 0; i < 100; i++) {
            if (fixed.evaluate("user", config).allowed()) {
                allowed++;
            }
        }

        System.out.println(
                "Fixed Window Allowed = " + allowed);
    }

    @Test
    void benchmarkMemoryUsage() {

        long before = MemoryUtil.usedMemoryInBytes();

        MutableClock clock = new MutableClock(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneOffset.UTC);

        FixedWindowStrategy strategy = new FixedWindowStrategy(
                new InMemoryRateLimitStore(),
                clock);

        RateLimitConfig config = new RateLimitConfig(
                100,
                Duration.ofMinutes(1));

        for (int i = 0; i < 100_000; i++) {
            strategy.evaluate("user-" + i, config);
        }

        System.gc();

        long after = MemoryUtil.usedMemoryInBytes();

        long used = after - before;

        System.out.println(
                "Memory used: "
                        + MemoryUtil.bytesToMB(used)
                        + " MB");
    }

    private BenchmarkResult benchmark(
            String algorithm,
            Object strategy,
            RateLimitConfig config) {

        long start = System.nanoTime();

        for (long i = 0; i < REQUESTS; i++) {

            String key = "user";

            if (strategy instanceof FixedWindowStrategy fixed) {
                fixed.evaluate(key, config);
            } else if (strategy instanceof SlidingWindowCounterStrategy sliding) {
                sliding.evaluate(key, config);
            }
        }

        long elapsed = System.nanoTime() - start;

        double rps = REQUESTS / (elapsed / 1_000_000_000.0);

        return new BenchmarkResult(
                algorithm,
                REQUESTS,
                elapsed,
                rps);
    }

    private void print(BenchmarkResult result) {

        System.out.println("==================================");
        System.out.println("Algorithm      : " + result.algorithm());
        System.out.println("Requests       : " + result.totalRequests());
        System.out.println("Elapsed (ms)   : "
                + result.elapsedNanos() / 1_000_000);
        System.out.println("Requests/sec   : "
                + String.format("%.2f", result.requestsPerSecond()));
        System.out.println("==================================");
    }
}