package com.poornima.ratelimiter.domain.model;

public record TokenBucketState(
        double tokens,
        long lastRefillTimestampMillis
) {
}