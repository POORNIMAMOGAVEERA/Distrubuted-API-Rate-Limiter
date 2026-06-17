package com.poornima.ratelimiter.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poornima.ratelimiter.domain.model.PolicyType;
import com.poornima.ratelimiter.domain.model.RateLimitPolicy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisRateLimitPolicyService
        implements RateLimitPolicyService {

    private static final String PREFIX = "policy:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRateLimitPolicyService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(RateLimitPolicy policy) {

        try {
            String key = buildKey(
                    policy.type(),
                    policy.identifier()
            );

            String value =
                    objectMapper.writeValueAsString(policy);

            redisTemplate.opsForValue()
                    .set(key, value);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to save policy",
                    e
            );
        }
    }

    @Override
    public RateLimitPolicy resolve(
            String userId,
            String api,
            String ip,
            String tier
    ) {

        RateLimitPolicy policy;

        policy = find(PolicyType.USER, userId);
        if (policy != null) {
            return policy;
        }

        policy = find(PolicyType.API, api);
        if (policy != null) {
            return policy;
        }

        policy = find(PolicyType.IP, ip);
        if (policy != null) {
            return policy;
        }

        policy = find(PolicyType.TIER, tier);
        if (policy != null) {
            return policy;
        }

        policy = find(PolicyType.DEFAULT, "default");
        if (policy != null) {
            return policy;
        }

        // Hard fallback
        return new RateLimitPolicy(
                PolicyType.DEFAULT,
                "default",
                100,
                Duration.ofMinutes(1)
        );
    }

    private RateLimitPolicy find(
            PolicyType type,
            String identifier
    ) {

        if (identifier == null || identifier.isBlank()) {
            return null;
        }

        String key = buildKey(type, identifier);

        String value =
                redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    value,
                    RateLimitPolicy.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to deserialize policy",
                    e
            );
        }
    }

    private String buildKey(
            PolicyType type,
            String identifier
    ) {
        return PREFIX
                + type.name().toLowerCase()
                + ":"
                + identifier;
    }
}