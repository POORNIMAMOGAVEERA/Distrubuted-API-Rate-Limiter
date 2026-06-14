package com.poornima.ratelimiter.domain.strategy;

import java.time.Clock;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.domain.model.SlidingWindowState;
import com.poornima.ratelimiter.domain.store.SlidingWindowStore;

public class SlidingWindowCounterStrategy
        implements RateLimiterStrategy {

    private final SlidingWindowStore store;
    private final Clock clock;

    public SlidingWindowCounterStrategy(
            SlidingWindowStore store,
            Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    @Override
    public RateLimitResult evaluate(
            String key,
            RateLimitConfig config) {

        long now = clock.millis();
        long windowSize = config.window().toMillis();

        SlidingWindowState state = store.get(key);

        if (state == null) {

            store.put(
                    key,
                    new SlidingWindowState(
                            now,
                            0,
                            1));

            return new RateLimitResult(
                    true,
                    config.limit() - 1,
                    0);
        }

        long elapsedMillis = now - state.windowStartMillis();

        long windowsPassed = elapsedMillis / windowSize;

        if (windowsPassed == 1) {

            state = new SlidingWindowState(
                    now,
                    state.currentWindowCount(),
                    0);

            store.put(key, state);

            elapsedMillis = 0;

        } else if (windowsPassed > 1) {

            state = new SlidingWindowState(
                    now,
                    0,
                    0);

            store.put(key, state);

            elapsedMillis = 0;
        }

        long remainingMillis = windowSize - elapsedMillis;

        double weight = (double) remainingMillis / windowSize;

        double estimatedCount = state.currentWindowCount()
                + (state.previousWindowCount() * weight);

        if (estimatedCount >= config.limit()) {

            long retryAfter = remainingMillis / 1000;

            return new RateLimitResult(
                    false,
                    0,
                    retryAfter);
        }

        long updatedCurrentCount = state.currentWindowCount() + 1;

        SlidingWindowState updatedState = new SlidingWindowState(
                state.windowStartMillis(),
                state.previousWindowCount(),
                updatedCurrentCount);

        store.put(key, updatedState);

        long remaining =
        Math.max(
                0,
                (long) (config.limit() - estimatedCount - 1)
        );

        return new RateLimitResult(
                true,
                remaining,
                0);
    }
}