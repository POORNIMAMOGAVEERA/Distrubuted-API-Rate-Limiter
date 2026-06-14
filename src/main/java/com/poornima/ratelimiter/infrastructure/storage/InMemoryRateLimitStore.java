package com.poornima.ratelimiter.infrastructure.storage;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.poornima.ratelimiter.domain.model.RateLimitState;
import com.poornima.ratelimiter.domain.store.RateLimitStore;

@Component
public class InMemoryRateLimitStore implements RateLimitStore {
    private final ConcurrentHashMap<String, RateLimitState> storage = new ConcurrentHashMap<>();

    @Override
    public RateLimitState get(String key){
        return storage.get(key);
    }

    @Override
    public void put(String key, RateLimitState state){
      storage.put(key, state);
    }
}
