package com.litellm.sdk.config;

import lombok.Builder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Builder(toBuilder = true)
public record CacheConfig(Boolean enabled, Duration ttl, Integer maxSize, TimeUnit timeUnit) {
    public CacheConfig(
            Boolean enabled,
            Duration ttl,
            Integer maxSize,
            TimeUnit timeUnit
    ) {
        this.enabled = enabled != null ? enabled : true;
        this.ttl = ttl != null ? ttl : Duration.ofMinutes(5);
        this.maxSize = maxSize != null ? maxSize : 1000;
        this.timeUnit = timeUnit != null ? timeUnit : TimeUnit.SECONDS;
    }

    public Duration getTtlInSeconds() {
        return Duration.ofSeconds(ttl.toSeconds());
    }
}
