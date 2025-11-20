package com.litellm.sdk.error;

import lombok.EqualsAndHashCode;

import java.time.Duration;

@EqualsAndHashCode(callSuper = true)
public class RateLimitException extends ProviderException {
    public final String providerId;
    public final Duration retryAfter;
    public final Integer rateLimitType;

    public RateLimitException(String message, String providerId, Duration retryAfter) {
        super(message, providerId, 429, "RATE_LIMIT_EXCEEDED");
        this.providerId = providerId;
        this.retryAfter = retryAfter;
        this.rateLimitType = null;
    }

    public RateLimitException(String message, String providerId, Duration retryAfter, Integer rateLimitType) {
        super(message, providerId, 429, "RATE_LIMIT_EXCEEDED");
        this.providerId = providerId;
        this.retryAfter = retryAfter;
        this.rateLimitType = rateLimitType;
    }

    public RateLimitException(String message, Throwable cause, String providerId, Duration retryAfter) {
        super(message, cause, providerId, 429, "RATE_LIMIT_EXCEEDED");
        this.providerId = providerId;
        this.retryAfter = retryAfter;
        this.rateLimitType = null;
    }
}
