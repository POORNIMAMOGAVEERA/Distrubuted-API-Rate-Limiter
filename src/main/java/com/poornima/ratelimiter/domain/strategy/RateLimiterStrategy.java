package com.poornima.ratelimiter.domain.strategy;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;

public interface RateLimiterStrategy {

    RateLimitResult evaluate(
            String key,
            RateLimitConfig config
    );
}
