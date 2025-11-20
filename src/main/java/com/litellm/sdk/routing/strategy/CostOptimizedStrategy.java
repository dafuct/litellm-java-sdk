package com.litellm.sdk.routing.strategy;

import com.litellm.sdk.config.RoutingStrategyConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.provider.Provider;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CostOptimizedStrategy implements RoutingStrategy {
    private final RoutingStrategyConfig config;
    private final Map<String, ProviderMetricsSnapshot> providerMetrics = new ConcurrentHashMap<>();

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

        if (providerMetrics.isEmpty()) {
            return healthyProviders.stream().findFirst();
        }

        return healthyProviders.stream()
            .min((p1, p2) -> {
                BigDecimal cost1 = getAverageCost(p1);
                BigDecimal cost2 = getAverageCost(p2);
                return cost1.compareTo(cost2);
            });
    }

    private BigDecimal getAverageCost(Provider provider) {
        ProviderMetricsSnapshot metrics = providerMetrics.get(provider.getName());
        if (metrics == null) {
            return new BigDecimal("1.0");
        }
        return new BigDecimal(metrics.costPerRequest());
    }

    @Override
    public RoutingStrategyConfig getConfig() {
        return config;
    }

    @Override
    public void updateProviderMetrics(String providerId, ProviderMetricsSnapshot snapshot) {
        providerMetrics.put(providerId, snapshot);
    }
}
