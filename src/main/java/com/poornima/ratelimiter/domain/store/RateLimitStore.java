package com.poornima.ratelimiter.domain.store;

import com.poornima.ratelimiter.domain.model.WindowState;

public interface RateLimitStore {
    WindowState get(String key);

    void put(String key, WindowState state);
}
