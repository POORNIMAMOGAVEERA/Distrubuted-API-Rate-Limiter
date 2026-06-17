package com.poornima.ratelimiter.application.service;

import com.poornima.ratelimiter.application.metrics.RateLimiterMetrics;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLuaFixedWindowRateLimiter {

    private static final String PREFIX = "rate_limit:fixed:";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> script;
    private final RateLimiterMetrics metrics;
    private final MeterRegistry meterRegistry;

    public RedisLuaFixedWindowRateLimiter(
            StringRedisTemplate redisTemplate,
            RedisScript<List> fixedWindowScript,
            RateLimiterMetrics metrics,
            MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.script = fixedWindowScript;
        this.metrics = metrics;
        this.meterRegistry = meterRegistry;
    }

    public RateLimitResult isAllowed(
            String key,
            long limit,
            Duration window) {

        Timer.Sample sample = Timer.start(meterRegistry);

        metrics.incrementRequests();

        try {

            String redisKey = PREFIX + key;

            List<Long> result;

            long redisStart = System.nanoTime();

            try {

                result = redisTemplate.execute(
                        script,
                        List.of(redisKey),
                        String.valueOf(limit),
                        String.valueOf(
                                window.getSeconds()));

            } finally {

                metrics.redisLatency()
                        .record(
                                System.nanoTime()
                                        - redisStart,
                                TimeUnit.NANOSECONDS);
            }

            if (result == null
                    || result.size() != 3) {

                throw new IllegalStateException(
                        "Unexpected Lua result");
            }

            boolean allowed = result.get(0).longValue() == 1L;

            long remaining = result.get(1);

            long retryAfter = result.get(2);

            if (allowed) {
                metrics.incrementAllowed();
            } else {
                metrics.incrementBlocked();
            }

            return new RateLimitResult(
                    allowed,
                    remaining,
                    retryAfter);

        } finally {

            sample.stop(
                    metrics.decisionLatency());
        }
    }
}