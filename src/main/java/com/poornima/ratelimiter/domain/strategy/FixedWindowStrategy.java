package com.poornima.ratelimiter.domain.strategy;

import java.time.Clock;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.domain.model.WindowState;
import com.poornima.ratelimiter.domain.store.RateLimitStore;

public class FixedWindowStrategy implements RateLimiterStrategy{
 
    private final RateLimitStore store;
    private final Clock clock;

    public FixedWindowStrategy(
        RateLimitStore store,
        Clock clock
    ){
        this.store = store;
        this.clock = clock;
    }

    @Override
    public RateLimitResult evaluate(
        String key, RateLimitConfig config
    ){
        long now = clock.millis();

        WindowState state = store.get(key);

        if(state == null){
            store.put(key, new WindowState(now, 1));

            return new RateLimitResult(
                true, config.limit()-1, 0
            );
        }

        long windowSize = config.window().toMillis();
        boolean expired = now - state.windowStartMillis() >= windowSize;

        if(expired) {
            store.put(key, new WindowState(now, 1));

            return new RateLimitResult(true, config.limit()-1, 0);
        }

        if(state.count()< config.limit()){
            long updatedCount = state.count()+1;

            store.put(key, new WindowState(state.windowStartMillis(), updatedCount));
            return new RateLimitResult(true, config.limit()-updatedCount, 0);
        }

        long retryAfter = (state.windowStartMillis()+windowSize - now)/1000;

        return new RateLimitResult(false, 0, retryAfter);
    }
}
