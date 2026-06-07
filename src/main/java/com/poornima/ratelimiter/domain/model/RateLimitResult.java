package com.poornima.ratelimiter.domain.model;

public record RateLimitResult(
        boolean allowed,
        long remaining,
        long retryAfterSeconds
) {
}
