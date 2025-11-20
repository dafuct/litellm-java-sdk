package com.litellm.sdk.config;

import lombok.Builder;

import java.time.Duration;

@Builder(toBuilder = true)
public record RateLimitConfig(Integer requestsPerMinute, Integer requestsPerSecond, Integer tokensPerMinute,
                              Duration cooldownPeriod) {
    public RateLimitConfig(
            Integer requestsPerMinute,
            Integer requestsPerSecond,
            Integer tokensPerMinute,
            Duration cooldownPeriod
    ) {
        this.requestsPerMinute = requestsPerMinute;
        this.requestsPerSecond = requestsPerSecond;
        this.tokensPerMinute = tokensPerMinute;
        this.cooldownPeriod = cooldownPeriod != null ? cooldownPeriod : Duration.ofSeconds(60);
    }
}
