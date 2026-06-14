package com.poornima.ratelimiter.domain.store;

import com.poornima.ratelimiter.domain.model.RateLimitState;

public interface RateLimitStore {
    RateLimitState get(String key);

    void put(String key, RateLimitState state);
}
