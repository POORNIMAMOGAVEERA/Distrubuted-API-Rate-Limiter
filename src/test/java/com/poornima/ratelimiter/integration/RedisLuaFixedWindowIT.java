package com.poornima.ratelimiter.integration;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.poornima.ratelimiter.application.service.RedisLuaFixedWindowRateLimiter;
import com.poornima.ratelimiter.domain.model.RateLimitResult;

@SpringBootTest
@Testcontainers
public class RedisLuaFixedWindowIT {

    @Autowired
    private RedisLuaFixedWindowRateLimiter limiter;

    @Test
    public void shouldEnforceLimitAtomically() {

        for (int i = 0; i < 5; i++) {
            RateLimitResult result =
                    limiter.isAllowed(
                            "user1",
                            5,
                            Duration.ofMinutes(1)
                    );

            assertTrue(result.allowed());
        }

        RateLimitResult rejected =
                limiter.isAllowed(
                        "user1",
                        5,
                        Duration.ofMinutes(1)
                );

        assertFalse(rejected.allowed());
    }
}