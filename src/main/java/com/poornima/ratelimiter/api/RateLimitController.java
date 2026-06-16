package com.poornima.ratelimiter.api;

import com.poornima.ratelimiter.domain.model.RateLimitConfig;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import com.poornima.ratelimiter.domain.strategy.FixedWindowStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class RateLimitController {

    private final FixedWindowStrategy strategy;

    public RateLimitController(
            FixedWindowStrategy strategy
    ) {
        this.strategy = strategy;
    }

    @GetMapping("/limit")
    public RateLimitResult limit(
            @RequestParam String key
    ) {

        return strategy.evaluate(
                key,
                new RateLimitConfig(
                        5,
                        Duration.ofMinutes(1)
                )
        );
    }
}