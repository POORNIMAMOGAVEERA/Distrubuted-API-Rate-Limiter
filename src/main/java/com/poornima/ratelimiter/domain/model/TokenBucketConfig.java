package com.poornima.ratelimiter.domain.model;

public record TokenBucketConfig(
        long capacity,
        double refillTokensPerSecond
) {
}