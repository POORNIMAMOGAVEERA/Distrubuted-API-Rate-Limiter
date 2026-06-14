package com.poornima.ratelimiter.domain.model;

public record SlidingWindowState(long windowStartMillis,
        long previousWindowCount,
        long currentWindowCount) {
    
}
