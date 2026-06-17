package com.poornima.ratelimiter.integration;

import com.poornima.ratelimiter.application.service.RedisAtomicFixedWindowRateLimiter;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RedisAtomicFixedWindowIT {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configure(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );
    }

    @Autowired
    RedisAtomicFixedWindowRateLimiter limiter;

    @Test
    void shouldEnforceLimit() {

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