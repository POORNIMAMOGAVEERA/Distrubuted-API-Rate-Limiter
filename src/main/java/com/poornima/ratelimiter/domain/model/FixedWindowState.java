package com.poornima.ratelimiter.domain.model;

public record FixedWindowState(long windowStartMillis, long count) implements RateLimitState{
    
}
