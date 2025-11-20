package com.litellm.sdk.routing.strategy;

import com.litellm.sdk.config.RoutingStrategyConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.provider.Provider;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class RoundRobinStrategy implements RoutingStrategy {
    private final RoutingStrategyConfig config;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Optional<Provider> selectProvider(List<Provider> availableProviders, ChatCompletionRequest request) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            return Optional.empty();
        }

        List<Provider> healthyProviders = availableProviders.stream()
            .filter(Provider::isHealthy)
            .toList();

        if (healthyProviders.isEmpty()) {
            return Optional.empty();
        }

        int index = counter.getAndIncrement() % healthyProviders.size();
        return Optional.of(healthyProviders.get(index));
    }

    @Override
    public RoutingStrategyConfig getConfig() {
        return config;
    }

    @Override
    public void updateProviderMetrics(String providerId, ProviderMetricsSnapshot snapshot) {
    }
}
