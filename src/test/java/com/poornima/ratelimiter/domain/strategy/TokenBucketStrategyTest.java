package com.poornima.ratelimiter.domain.strategy;

import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.domain.model.TokenBucketConfig;
import com.poornima.ratelimiter.infrastructure.storage.InMemoryTokenBucketStore;
import com.poornima.ratelimiter.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketStrategyTest {

    private static final String KEY = "user-123";

    private TokenBucketStrategy strategy;
    private MutableClock clock;

    @BeforeEach
    void setUp() {

        clock = new MutableClock(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        strategy = new TokenBucketStrategy(
                new InMemoryTokenBucketStore(),
                clock
        );
    }

    @Test
    void shouldAllowRequestsWithinCapacity() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        5,
                        1.0
                );

        for (int i = 0; i < 5; i++) {

            RateLimitResult result =
                    strategy.evaluate(KEY, config);

            assertTrue(
                    result.allowed(),
                    "Request " + (i + 1) + " should be allowed"
            );
        }
    }

    @Test
    void shouldRejectWhenBucketIsEmpty() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        2,
                        1.0
                );

        strategy.evaluate(KEY, config);
        strategy.evaluate(KEY, config);

        RateLimitResult result =
                strategy.evaluate(KEY, config);

        assertFalse(result.allowed());
        assertTrue(result.retryAfterSeconds() > 0);
    }

    @Test
    void shouldRefillTokensOverTime() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        2,
                        1.0
                );

        strategy.evaluate(KEY, config);
        strategy.evaluate(KEY, config);

        assertFalse(
                strategy.evaluate(KEY, config)
                        .allowed()
        );

        clock.advance(Duration.ofSeconds(1));

        RateLimitResult result =
                strategy.evaluate(KEY, config);

        assertTrue(result.allowed());
    }

    @Test
    void shouldRefillFractionalTokens() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        1,
                        2.0 // 2 tokens/sec
                );

        strategy.evaluate(KEY, config);

        assertFalse(
                strategy.evaluate(KEY, config)
                        .allowed()
        );

        clock.advance(Duration.ofMillis(500));

        RateLimitResult result =
                strategy.evaluate(KEY, config);

        assertTrue(result.allowed());
    }

    @Test
    void shouldNeverExceedCapacity() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        10,
                        100.0
                );

        clock.advance(Duration.ofHours(1));

        RateLimitResult result =
                strategy.evaluate(KEY, config);

        assertTrue(result.allowed());

        // Bucket should have been capped at capacity
        assertTrue(result.remaining() <= 9);
    }

    @Test
    void shouldCalculateRemainingTokensCorrectly() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        5,
                        1.0
                );

        RateLimitResult first =
                strategy.evaluate(KEY, config);

        RateLimitResult second =
                strategy.evaluate(KEY, config);

        assertEquals(4, first.remaining());
        assertEquals(3, second.remaining());
    }

    @Test
    void shouldReturnRetryAfterWhenRejected() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        1,
                        1.0
                );

        strategy.evaluate(KEY, config);

        RateLimitResult result =
                strategy.evaluate(KEY, config);

        assertFalse(result.allowed());
        assertEquals(1, result.retryAfterSeconds());
    }

    @Test
    void shouldRefillMultipleTokens() {

        TokenBucketConfig config =
                new TokenBucketConfig(
                        5,
                        2.0 // 2 tokens/sec
                );

        for (int i = 0; i < 5; i++) {
            strategy.evaluate(KEY, config);
        }

        assertFalse(
                strategy.evaluate(KEY, config)
                        .allowed()
        );

        clock.advance(Duration.ofSeconds(2));

        RateLimitResult first =
                strategy.evaluate(KEY, config);

        RateLimitResult second =
                strategy.evaluate(KEY, config);

        assertTrue(first.allowed());
        assertTrue(second.allowed());
    }
}