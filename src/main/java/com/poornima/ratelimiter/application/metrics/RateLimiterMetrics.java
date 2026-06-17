package com.poornima.ratelimiter.application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterMetrics {

    private final Counter requestsTotal;
    private final Counter requestsAllowed;
    private final Counter requestsBlocked;

    private final Timer decisionLatency;
    private final Timer redisLatency;

    public RateLimiterMetrics(
            MeterRegistry registry
    ) {

        this.requestsTotal =
                Counter.builder("requests_total")
                        .description("Total requests")
                        .register(registry);

        this.requestsAllowed =
                Counter.builder("requests_allowed_total")
                        .description("Allowed requests")
                        .register(registry);

        this.requestsBlocked =
                Counter.builder("requests_blocked_total")
                        .description("Blocked requests")
                        .register(registry);

        this.decisionLatency =
                Timer.builder(
                                "rate_limit_decision_latency")
                        .description(
                                "Rate limit decision latency")
                        .publishPercentileHistogram()
                        .register(registry);

        this.redisLatency =
                Timer.builder("redis_latency")
                        .description(
                                "Redis operation latency")
                        .publishPercentileHistogram()
                        .register(registry);
    }

    public void incrementRequests() {
        requestsTotal.increment();
    }

    public void incrementAllowed() {
        requestsAllowed.increment();
    }

    public void incrementBlocked() {
        requestsBlocked.increment();
    }

    public Timer decisionLatency() {
        return decisionLatency;
    }

    public Timer redisLatency() {
        return redisLatency;
    }
}