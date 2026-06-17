package com.poornima.ratelimiter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class LuaScriptConfig {

    @Bean
    public DefaultRedisScript<List> fixedWindowScript() {

        DefaultRedisScript<List> script =
                new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource(
                        "scripts/fixed_window.lua"
                )
        );

        script.setResultType(List.class);

        return script;
    }
}