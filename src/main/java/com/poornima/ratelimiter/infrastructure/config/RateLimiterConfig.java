package com.poornima.ratelimiter.infrastructure.config;

import com.poornima.ratelimiter.domain.strategy.FixedWindowStrategy;
import com.poornima.ratelimiter.infrastructure.storage.RedisFixedWindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public FixedWindowStrategy fixedWindowStrategy(
            RedisFixedWindowStore store,
            Clock clock
    ) {
        return new FixedWindowStrategy(
                store,
                clock
        );
    }
}