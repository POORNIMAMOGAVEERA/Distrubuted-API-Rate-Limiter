package com.poornima.ratelimiter.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.poornima.ratelimiter.application.service.RateLimitPolicyService;
import com.poornima.ratelimiter.domain.model.PolicyType;
import com.poornima.ratelimiter.domain.model.RateLimitPolicy;

@SpringBootTest
@Testcontainers
class RedisRateLimitPolicyServiceIT {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedis(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.redis.host",
                redis::getHost);

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379));
    }

    @Autowired
    private RateLimitPolicyService service;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanup() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    void shouldPreferUserPolicy() {

        service.save(
                new RateLimitPolicy(
                        PolicyType.TIER,
                        "PREMIUM",
                        1000,
                        Duration.ofMinutes(1)));

        service.save(
                new RateLimitPolicy(
                        PolicyType.USER,
                        "user123",
                        500,
                        Duration.ofMinutes(1)));

        RateLimitPolicy resolved = service.resolve(
                "user123",
                null,
                null,
                "PREMIUM");

        assertEquals(500, resolved.limit());
        assertEquals(
                PolicyType.USER,
                resolved.type());
    }

    @Test
    void shouldFallbackToTierPolicy() {

        service.save(
                new RateLimitPolicy(
                        PolicyType.TIER,
                        "PREMIUM",
                        1000,
                        Duration.ofMinutes(1)));

        RateLimitPolicy resolved = service.resolve(
                null,
                null,
                null,
                "PREMIUM");

        assertEquals(1000, resolved.limit());
        assertEquals(
                PolicyType.TIER,
                resolved.type());
    }

    @Test
    void shouldFallbackToDefaultPolicy() {

        service.save(
                new RateLimitPolicy(
                        PolicyType.DEFAULT,
                        "default",
                        100,
                        Duration.ofMinutes(1)));

        RateLimitPolicy resolved = service.resolve(
                null,
                null,
                null,
                null);

        assertEquals(100, resolved.limit());
        assertEquals(
                PolicyType.DEFAULT,
                resolved.type());
    }
}