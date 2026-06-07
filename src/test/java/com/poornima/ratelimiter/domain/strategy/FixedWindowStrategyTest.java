package com.poornima.ratelimiter.domain.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.infrastructure.storage.InMemoryRateLimitStore;
import com.poornima.ratelimiter.support.MutableClock;

public class FixedWindowStrategyTest {

    private FixedWindowStrategy strategy;

    private MutableClock clock;

    private static final String KEY = "user-123";

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);

        strategy = new FixedWindowStrategy(
                new InMemoryRateLimitStore(),
                clock);
    }

    private RateLimitConfig config() {
        return new RateLimitConfig(5, Duration.ofMinutes(1));
    }

    @Test
    void shouldAllowFirstRequest() {
        RateLimitResult result = strategy.evaluate(KEY, config());

        assertTrue(result.allowed());
        assertEquals(4, result.remaining());
        assertEquals(0, result.retryAfterSeconds());
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = strategy.evaluate(KEY, config());
            assertTrue(
                    result.allowed(),
                    "Request " + i + " should be allowed");
        }
    }

    @Test
    void shouldRejectRequestWhenLimitExceeded() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertFalse(result.allowed());
        assertEquals(0, result.remaining());
    }

    @Test
    void shouldReturnRetryAfterWhenRejected() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertFalse(result.allowed());
        assertTrue(result.retryAfterSeconds() > 0);
    }

    @Test
    void shouldResetWindowAfterExpiration() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        clock.advance(Duration.ofMinutes(1));

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertTrue(result.allowed());
        assertEquals(4, result.remaining());
    }

    @Test
    void shouldTrackRemainingRequestsCorrectly() {

        RateLimitResult first = strategy.evaluate(KEY, config());

        RateLimitResult second = strategy.evaluate(KEY, config());

        RateLimitResult third = strategy.evaluate(KEY, config());

        assertEquals(4, first.remaining());
        assertEquals(3, second.remaining());
        assertEquals(2, third.remaining());
    }

    @Test
    void shouldResetExactlyAtWindowBoundary() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        clock.advance(Duration.ofSeconds(60));

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertTrue(result.allowed());
        assertEquals(4, result.remaining());
    }
}
