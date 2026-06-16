package com.poornima.ratelimiter.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisConnectivityVerifier implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    public RedisConnectivityVerifier(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {

        redisTemplate.opsForValue()
                .set("redis:test", "connected");

        String value =
                redisTemplate.opsForValue()
                        .get("redis:test");

        System.out.println(
                "Redis connectivity test: " + value
        );
    }
}