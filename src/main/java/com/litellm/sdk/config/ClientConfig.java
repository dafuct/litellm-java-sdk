package com.litellm.sdk.config;

import lombok.Builder;
import lombok.With;

import java.time.Duration;
import java.util.List;

@With
@Builder(toBuilder = true)
public record ClientConfig(List<ProviderConfig> providers, RoutingStrategyConfig routingStrategy, CacheConfig cache,
                           RetryConfig retry, Duration timeout, String environmentPrefix) {
    public void validate() {
        if (providers == null || providers.isEmpty()) {
            throw new IllegalArgumentException("At least one provider must be configured");
        }

        boolean hasEnabledProvider = providers.stream()
                .anyMatch(ProviderConfig::enabled);

        if (!hasEnabledProvider) {
            throw new IllegalArgumentException("At least one provider must be enabled");
        }

    }

    public ProviderConfig getProvider(String providerId) {
        return providers.stream()
                .filter(p -> p.id().equals(providerId))
                .findFirst()
                .orElse(null);
    }

    public List<ProviderConfig> getEnabledProviders() {
        return providers.stream()
                .filter(ProviderConfig::enabled)
                .toList();
    }
}
