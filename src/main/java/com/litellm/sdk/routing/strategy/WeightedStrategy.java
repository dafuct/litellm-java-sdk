package com.litellm.sdk.routing.strategy;

import com.litellm.sdk.config.RoutingStrategyConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.provider.Provider;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class WeightedStrategy implements RoutingStrategy {
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

        int totalWeight = healthyProviders.stream()
            .mapToInt(this::getProviderWeight)
            .sum();

        if (totalWeight <= 0) {
            return healthyProviders.stream().findFirst();
        }

        int index = counter.getAndIncrement() % totalWeight;
        int cumulativeWeight = 0;

        for (Provider provider : healthyProviders) {
            cumulativeWeight += getProviderWeight(provider);
            if (index < cumulativeWeight) {
                return Optional.of(provider);
            }
        }

        return Optional.of(healthyProviders.get(healthyProviders.size() - 1));
    }

    private int getProviderWeight(Provider provider) {
        try {
            String weightParam = (String) config.parameters().get("weight_" + provider.getName());
            return weightParam != null ? Integer.parseInt(weightParam) : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public RoutingStrategyConfig getConfig() {
        return config;
    }

    @Override
    public void updateProviderMetrics(String providerId, ProviderMetricsSnapshot snapshot) {
    }
}
