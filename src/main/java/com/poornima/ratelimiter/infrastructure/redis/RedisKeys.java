package com.poornima.ratelimiter.infrastructure.redis;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static String fixedWindow(String key) {
        return "rate_limit:fixed:" + key;
    }

    public static String slidingWindow(String key) {
        return "rate_limit:sliding:" + key;
    }

    public static String tokenBucket(String key) {
        return "rate_limit:token:" + key;
    }
}