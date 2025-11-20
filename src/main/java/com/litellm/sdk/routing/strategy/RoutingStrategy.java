package com.litellm.sdk.routing.strategy;

import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.config.RoutingStrategyConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;

import java.util.List;
import java.util.Optional;

public interface RoutingStrategy {
    Optional<Provider> selectProvider(List<Provider> availableProviders, ChatCompletionRequest request);

    RoutingStrategyConfig getConfig();

    void updateProviderMetrics(String providerId, ProviderMetricsSnapshot snapshot);

    record ProviderMetricsSnapshot(String providerId, double averageLatencyMs, double successRate,
                                   double costPerRequest) {
    }
}
