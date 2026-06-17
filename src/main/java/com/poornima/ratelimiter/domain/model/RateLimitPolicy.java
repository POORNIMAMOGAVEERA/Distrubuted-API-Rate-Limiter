package com.poornima.ratelimiter.domain.model;

import java.time.Duration;

public record RateLimitPolicy(

        PolicyType type,

        String identifier,

        long limit,

        Duration window
) {
}