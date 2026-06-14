package com.poornima.ratelimiter.domain.strategy;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.infrastructure.storage.InMemorySlidingWindowStore;
import com.poornima.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterStrategyTest {

    private static final String KEY = "user-123";

    private SlidingWindowCounterStrategy strategy;
    private MutableClock clock;

    @BeforeEach
    void setUp() {

        clock = new MutableClock(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneOffset.UTC);

        strategy = new SlidingWindowCounterStrategy(
                new InMemorySlidingWindowStore(),
                clock);
    }

    private RateLimitConfig config() {

        return new RateLimitConfig(
                5,
                Duration.ofMinutes(1));
    }

    @Test
    void shouldAllowFirstRequest() {

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertTrue(result.allowed());
        assertEquals(4, result.remaining());
    }

    @Test
    void shouldAllowRequestsWithinLimit() {

        for (int i = 0; i < 5; i++) {

            RateLimitResult result = strategy.evaluate(KEY, config());

            assertTrue(result.allowed());
        }
    }

    @Test
    void shouldRejectWhenLimitExceeded() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertFalse(result.allowed());
    }

    // @Test
    // void shouldResetWindowAfterExpiration() {

    //     for (int i = 0; i < 5; i++) {
    //         strategy.evaluate(KEY, config());
    //     }

    //     clock.advance(Duration.ofMinutes(1));

    //     RateLimitResult result = strategy.evaluate(KEY, config());

    //     assertTrue(result.allowed());
    // }

    @Test
    void shouldTrackRemainingRequests() {

        RateLimitResult first = strategy.evaluate(KEY, config());

        RateLimitResult second = strategy.evaluate(KEY, config());

        assertEquals(4, first.remaining());
        assertEquals(3, second.remaining());
    }

    @Test
    void shouldCarryPreviousWindowTrafficIntoNextWindow() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        clock.advance(Duration.ofMinutes(1));

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertFalse(result.allowed());
    }

    @Test
    void shouldAllowRequestsAfterMultipleWindowsPass() {

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config());
        }

        clock.advance(Duration.ofMinutes(2));

        RateLimitResult result = strategy.evaluate(KEY, config());

        assertTrue(result.allowed());
    }
}