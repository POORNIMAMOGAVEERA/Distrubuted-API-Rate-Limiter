package com.poornima.ratelimiter.application.service;

import com.poornima.ratelimiter.domain.model.RateLimitPolicy;

public interface RateLimitPolicyService {

    void save(
            RateLimitPolicy policy
    );

    RateLimitPolicy resolve(
            String userId,
            String api,
            String ip,
            String tier
    );
}
