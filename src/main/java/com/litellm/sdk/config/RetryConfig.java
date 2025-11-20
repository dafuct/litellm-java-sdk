package com.litellm.sdk.config;

import lombok.Builder;

import java.time.Duration;

@Builder(toBuilder = true)
public record RetryConfig(Integer maxAttempts, Duration initialDelay, Duration maxDelay,
                          BackoffStrategy backoffStrategy, Double exponentialBase, Boolean jitter,
                          Integer maxBackoffFactor) {
    public RetryConfig(
            Integer maxAttempts,
            Duration initialDelay,
            Duration maxDelay,
            BackoffStrategy backoffStrategy,
            Double exponentialBase,
            Boolean jitter,
            Integer maxBackoffFactor
    ) {
        this.maxAttempts = maxAttempts != null ? maxAttempts : 3;
        this.initialDelay = initialDelay != null ? initialDelay : Duration.ofSeconds(1);
        this.maxDelay = maxDelay != null ? maxDelay : Duration.ofSeconds(60);
        this.backoffStrategy = backoffStrategy != null ? backoffStrategy : BackoffStrategy.EXPONENTIAL;
        this.exponentialBase = exponentialBase != null ? exponentialBase : 2.0;
        this.jitter = jitter != null ? jitter : true;
        this.maxBackoffFactor = maxBackoffFactor != null ? maxBackoffFactor : 10;

        if (this.maxAttempts < 1) {
            throw new IllegalArgumentException("Max attempts must be at least 1");
        }
        if (this.initialDelay.isNegative() || this.initialDelay.isZero()) {
            throw new IllegalArgumentException("Initial delay must be positive");
        }
        if (this.maxDelay.compareTo(this.initialDelay) < 0) {
            throw new IllegalArgumentException("Max delay must be greater than or equal to initial delay");
        }
    }

    public enum BackoffStrategy {
        FIXED,
        EXPONENTIAL,
        LINEAR,
        EXPONENTIAL_WITH_JITTER
    }
}
