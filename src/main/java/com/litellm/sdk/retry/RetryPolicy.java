package com.litellm.sdk.retry;

import com.litellm.sdk.config.RetryConfig;
import com.litellm.sdk.error.ProviderException;
import com.litellm.sdk.error.RateLimitException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Random;
import java.util.function.Predicate;

public class RetryPolicy {
    private final RetryConfig config;
    private final Random random = new Random();

    public RetryPolicy(RetryConfig config) {
        this.config = config;
    }

    public Retry buildRetry(Predicate<Throwable> retryableErrorPredicate) {
        return Retry.backoff(config.maxAttempts() - 1, config.initialDelay())
            .maxBackoff(config.maxDelay())
            .doBeforeRetry(retryBackoff -> {
                int attempt = (int) retryBackoff.totalRetries() + 1;
                if (attempt >= config.maxAttempts()) {
                    throw new RuntimeException("Max retry attempts exceeded");
                }
            })
            .filter(retryableErrorPredicate)
            .jitter(config.jitter() ? 0.1 : 0.0);
    }

    public Duration calculateDelay(int attempt) {
        Duration baseDelay = config.initialDelay();
        double multiplier = Math.pow(config.exponentialBase(), attempt - 1);
        long delayMs = (long) (baseDelay.toMillis() * multiplier);

        delayMs = Math.min(delayMs, config.maxDelay().toMillis());

        if (config.jitter()) {
            double jitterRange = 0.1;
            double jitter = (random.nextDouble() - 0.5) * 2 * jitterRange;
            delayMs = (long) (delayMs * (1 + jitter));
        }

        return Duration.ofMillis(Math.max(delayMs, baseDelay.toMillis()));
    }

    public boolean isRetryable(Throwable error) {
        if (error instanceof RateLimitException) {
            return true;
        }
        if (error instanceof ProviderException) {
            return ((ProviderException) error).isRetryable();
        }
        return false;
    }
}
