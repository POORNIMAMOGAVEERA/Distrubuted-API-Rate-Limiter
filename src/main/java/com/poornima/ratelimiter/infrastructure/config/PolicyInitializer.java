package com.poornima.ratelimiter.infrastructure.config;

import com.poornima.ratelimiter.application.service.RateLimitPolicyService;
import com.poornima.ratelimiter.domain.model.PolicyType;
import com.poornima.ratelimiter.domain.model.RateLimitPolicy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PolicyInitializer
        implements CommandLineRunner {

    private final RateLimitPolicyService service;

    public PolicyInitializer(
            RateLimitPolicyService service
    ) {
        this.service = service;
    }

    @Override
    public void run(String... args) {

        service.save(
                new RateLimitPolicy(
                        PolicyType.DEFAULT,
                        "default",
                        100,
                        Duration.ofMinutes(1)
                )
        );

        service.save(
                new RateLimitPolicy(
                        PolicyType.TIER,
                        "FREE",
                        100,
                        Duration.ofMinutes(1)
                )
        );

        service.save(
                new RateLimitPolicy(
                        PolicyType.TIER,
                        "PREMIUM",
                        1000,
                        Duration.ofMinutes(1)
                )
        );

        service.save(
                new RateLimitPolicy(
                        PolicyType.TIER,
                        "ENTERPRISE",
                        10000,
                        Duration.ofMinutes(1)
                )
        );
    }
}