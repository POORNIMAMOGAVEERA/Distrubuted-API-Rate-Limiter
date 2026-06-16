package com.poornima.ratelimiter.infrastructure.storage;

import com.poornima.ratelimiter.domain.model.FixedWindowState;
import com.poornima.ratelimiter.domain.model.RateLimitState;
import com.poornima.ratelimiter.domain.store.RateLimitStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisFixedWindowStore implements RateLimitStore {

    private static final String PREFIX =
            "rate_limit:fixed:";

    private final StringRedisTemplate redisTemplate;

    public RedisFixedWindowStore(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitState get(String key) {

        String redisKey = PREFIX + key;

        String value =
                redisTemplate.opsForValue()
                        .get(redisKey);

        if (value == null) {
            return null;
        }

        String[] parts = value.split(":");

        long count =
                Long.parseLong(parts[0]);

        long windowStart =
                Long.parseLong(parts[1]);

        return new FixedWindowState(
                windowStart,
                count
        );
    }

    @Override
    public void put(
            String key,
            RateLimitState state
    ) {

        if (!(state instanceof FixedWindowState fixedState)) {
            throw new IllegalArgumentException(
                    "Expected FixedWindowState"
            );
        }

        String redisKey = PREFIX + key;

        String value =
                fixedState.count()
                        + ":"
                        + fixedState.windowStartMillis();

        redisTemplate.opsForValue().set(
                redisKey,
                value,
                Duration.ofMinutes(1)
        );
    }
}