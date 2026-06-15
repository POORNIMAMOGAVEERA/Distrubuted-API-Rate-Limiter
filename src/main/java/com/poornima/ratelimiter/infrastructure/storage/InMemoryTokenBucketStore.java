package com.poornima.ratelimiter.infrastructure.storage;

import com.poornima.ratelimiter.domain.model.TokenBucketState;
import com.poornima.ratelimiter.domain.store.TokenBucketStore;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryTokenBucketStore
        implements TokenBucketStore {

    private final ConcurrentMap<String, TokenBucketState> storage =
            new ConcurrentHashMap<>();

    @Override
    public TokenBucketState get(String key) {
        return storage.get(key);
    }

    @Override
    public void put(
            String key,
            TokenBucketState state
    ) {
        storage.put(key, state);
    }
}