package com.poornima.ratelimiter.domain.model;

import java.time.Duration;

public record RateLimitConfig(long limit,Duration window) {
}
