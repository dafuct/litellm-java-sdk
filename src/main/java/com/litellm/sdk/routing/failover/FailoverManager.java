package com.litellm.sdk.routing.failover;

import com.litellm.sdk.config.RetryConfig;
import com.litellm.sdk.error.ProviderException;
import com.litellm.sdk.error.RateLimitException;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.provider.ProviderHealth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class FailoverManager {
    private final RetryConfig retryConfig;
    private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    public <T> Mono<T> executeWithFailover(List<Provider> providers, Mono<T> request) {
        return request
            .onErrorResume(throwable -> handleFailure(providers, throwable));
    }

    private <T> Mono<T> handleFailure(List<Provider> providers, Throwable throwable) {
        if (throwable instanceof RateLimitException rateLimitException) {
            return handleRateLimit(providers, rateLimitException);
        } else if (throwable instanceof ProviderException providerException) {
            return handleProviderFailure(providers, providerException);
        } else {
            log.warn("Unexpected error during request: {}", throwable.getMessage());
            return Mono.error(throwable);
        }
    }

    private <T> Mono<T> handleRateLimit(List<Provider> providers, RateLimitException rateLimitException) {
        String providerId = rateLimitException.providerId;
        log.warn("Rate limit hit for provider: {}. Waiting {} before retry",
            providerId, rateLimitException.retryAfter);

        circuitBreakers.putIfAbsent(providerId, new CircuitBreakerState());

        return Mono.delay(rateLimitException.retryAfter)
            .flatMap(ignored -> Mono.<T>error(new RuntimeException("Retry after rate limit")));
    }

    private <T> Mono<T> handleProviderFailure(List<Provider> providers, ProviderException providerException) {
        String providerId = providerException.providerId;
        log.warn("Provider failure for {}: {}", providerId, providerException.getMessage());

        CircuitBreakerState state = circuitBreakers.computeIfAbsent(providerId, k -> new CircuitBreakerState());
        state.recordFailure();

        if (state.isOpen()) {
            state.markUnhealthy();
            log.error("Circuit breaker opened for provider: {}", providerId);
            return Mono.error(new RuntimeException("Circuit breaker opened for provider"));
        }

        return Mono.error(providerException);
    }

    private static class CircuitBreakerState {
        private int failureCount = 0;
        private long lastFailureTime = 0;
        private static final int FAILURE_THRESHOLD = 5;
        private static final Duration OPEN_DURATION = Duration.ofSeconds(60);

        public void recordFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
        }

        public boolean isOpen() {
            return failureCount >= FAILURE_THRESHOLD &&
                System.currentTimeMillis() - lastFailureTime < OPEN_DURATION.toMillis();
        }

        public void markUnhealthy() {
        }
    }
}
