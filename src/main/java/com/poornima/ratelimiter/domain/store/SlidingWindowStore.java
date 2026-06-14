package com.poornima.ratelimiter.domain.store;

import com.poornima.ratelimiter.domain.model.SlidingWindowState;

public interface SlidingWindowStore  {
    
    SlidingWindowState get(String key);

    void put(String key, SlidingWindowState state);
}
