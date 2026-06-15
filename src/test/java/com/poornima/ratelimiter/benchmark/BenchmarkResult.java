package com.poornima.ratelimiter.benchmark;

public record BenchmarkResult(
        String algorithm,
        long totalRequests,
        long elapsedNanos,
        double requestsPerSecond
) {
}