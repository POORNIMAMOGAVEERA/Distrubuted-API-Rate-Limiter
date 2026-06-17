package com.poornima.ratelimiter.api;

import com.poornima.ratelimiter.application.service.RedisAtomicFixedWindowRateLimiter;
import com.poornima.ratelimiter.application.service.RedisLuaFixedWindowRateLimiter;
import com.poornima.ratelimiter.domain.model.RateLimitResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class AtomicRateLimitController {

    private final RedisLuaFixedWindowRateLimiter limiter;

    public AtomicRateLimitController(
            RedisLuaFixedWindowRateLimiter limiter) {
        this.limiter = limiter;
    }

    // @GetMapping("/atomic-limit")
    // public RateLimitResult limit(
    //         @RequestParam String key) {

    //     return limiter.isAllowed(
    //             key,
    //             5,
    //             Duration.ofMinutes(1));
    // }

    @GetMapping("/lua-limit")
    public RateLimitResult limit(
            @RequestParam String key) {
        return limiter.isAllowed(
                key,
                5,
                Duration.ofMinutes(1));
    }
}