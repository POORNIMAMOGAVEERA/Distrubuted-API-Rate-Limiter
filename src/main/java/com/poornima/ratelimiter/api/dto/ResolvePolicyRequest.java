package com.poornima.ratelimiter.api.dto;

public record ResolvePolicyRequest(

        String userId,

        String api,

        String ip,

        String tier
) {
}