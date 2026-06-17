package com.poornima.ratelimiter.application.service;

import com.poornima.ratelimiter.domain.model.RateLimitResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisAtomicFixedWindowRateLimiter {

    private static final String PREFIX =
            "rate_limit:fixed:";

    private final StringRedisTemplate redisTemplate;

    public RedisAtomicFixedWindowRateLimiter(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitResult isAllowed(
            String key,
            long limit,
            Duration window
    ) {

        String redisKey = PREFIX + key;

        Long count =
                redisTemplate.opsForValue()
                        .increment(redisKey);

        if (count == null) {
            throw new IllegalStateException(
                    "Redis increment returned null"
            );
        }

        if (count == 1) {

            redisTemplate.expire(
                    redisKey,
                    window
            );
        }

        boolean allowed = count <= limit;

        long remaining =
                Math.max(0, limit - count);

        long retryAfter = 0;

        if (!allowed) {

            Long ttl =
                    redisTemplate.getExpire(redisKey);

            retryAfter =
                    ttl != null && ttl > 0
                            ? ttl
                            : 0;
        }

        return new RateLimitResult(
                allowed,
                remaining,
                retryAfter
        );
    }
}