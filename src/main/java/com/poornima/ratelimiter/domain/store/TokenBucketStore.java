package com.poornima.ratelimiter.domain.store;

import com.poornima.ratelimiter.domain.model.TokenBucketState;

public interface TokenBucketStore {

    TokenBucketState get(String key);

    void put(String key, TokenBucketState state);
}