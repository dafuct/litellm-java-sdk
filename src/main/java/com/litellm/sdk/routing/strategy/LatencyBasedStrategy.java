package com.litellm.sdk.routing.strategy;

import com.litellm.sdk.config.RoutingStrategyConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.provider.Provider;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class LatencyBasedStrategy implements RoutingStrategy {
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
                Duration latency1 = getAverageLatency(p1);
                Duration latency2 = getAverageLatency(p2);
                return latency1.compareTo(latency2);
            });
    }

    private Duration getAverageLatency(Provider provider) {
        ProviderMetricsSnapshot metrics = providerMetrics.get(provider.getName());
        if (metrics == null) {
            return Duration.ofMillis(1000);
        }
        return Duration.ofMillis((long) metrics.averageLatencyMs());
    }

    @Override
    public RoutingStrategyConfig getConfig() {
        return config;
    }

    @Override
    public void updateProviderMetrics(String providerId, ProviderMetricsSnapshot snapshot) {
        providerMetrics.put(providerId, snapshot);
    }

    Map<String, ProviderMetricsSnapshot> getProviderMetrics() {
        return providerMetrics;
    }
}
