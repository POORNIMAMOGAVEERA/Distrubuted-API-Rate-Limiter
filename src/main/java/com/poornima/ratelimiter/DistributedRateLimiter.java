package com.poornima.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistributedRateLimiter {

	public static void main(String[] args) {
		SpringApplication.run(DistributedRateLimiter.class, args);
	}

}
