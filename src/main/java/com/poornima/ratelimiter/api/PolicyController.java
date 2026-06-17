package com.poornima.ratelimiter.api;

import com.poornima.ratelimiter.application.service.RateLimitPolicyService;
import com.poornima.ratelimiter.domain.model.RateLimitPolicy;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/policy")
public class PolicyController {

    private final RateLimitPolicyService service;

    public PolicyController(
            RateLimitPolicyService service
    ) {
        this.service = service;
    }

    @PostMapping
    public void createPolicy(
            @RequestBody RateLimitPolicy policy
    ) {
        service.save(policy);
    }

    @GetMapping("/resolve")
    public RateLimitPolicy resolve(
            @RequestParam(required = false)
            String userId,

            @RequestParam(required = false)
            String api,

            @RequestParam(required = false)
            String ip,

            @RequestParam(required = false)
            String tier
    ) {

        return service.resolve(
                userId,
                api,
                ip,
                tier
        );
    }
}