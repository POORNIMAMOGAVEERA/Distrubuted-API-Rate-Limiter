package com.poornima.ratelimiter.domain.strategy;

import com.poornima.ratelimiter.domain.model.*;
import com.poornima.ratelimiter.domain.store.TokenBucketStore;

import java.time.Clock;

public class TokenBucketStrategy {

    private final TokenBucketStore store;
    private final Clock clock;

    public TokenBucketStrategy(
            TokenBucketStore store,
            Clock clock
    ) {
        this.store = store;
        this.clock = clock;
    }

    public RateLimitResult evaluate(
            String key,
            TokenBucketConfig config
    ) {

        long now = clock.millis();

        TokenBucketState state = store.get(key);

        if (state == null) {

            state = new TokenBucketState(
                    config.capacity(),
                    now
            );
        }

        double elapsedSeconds =
                (now - state.lastRefillTimestampMillis())
                        / 1000.0;

        double newTokens =
                elapsedSeconds
                        * config.refillTokensPerSecond();

        double availableTokens =
                Math.min(
                        config.capacity(),
                        state.tokens() + newTokens
                );

        if (availableTokens < 1) {

            long retryAfter =
                    (long) Math.ceil(
                            (1 - availableTokens)
                                    / config.refillTokensPerSecond()
                    );

            store.put(
                    key,
                    new TokenBucketState(
                            availableTokens,
                            now
                    )
            );

            return new RateLimitResult(
                    false,
                    0,
                    retryAfter
            );
        }

        availableTokens--;

        store.put(
                key,
                new TokenBucketState(
                        availableTokens,
                        now
                )
        );

        return new RateLimitResult(
                true,
                (long) availableTokens,
                0
        );
    }
}