package com.litellm.sdk.config;

import lombok.Builder;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record ProviderConfig(String id, String name, String apiKey, String baseUrl, List<String> models, Integer weight,
                             Duration timeout, RateLimitConfig rateLimit, Boolean enabled, Integer priority,
                             Map<String, Object> customParameters) {
    public ProviderConfig(
            String id,
            String name,
            String apiKey,
            String baseUrl,
            List<String> models,
            Integer weight,
            Duration timeout,
            RateLimitConfig rateLimit,
            Boolean enabled,
            Integer priority,
            Map<String, Object> customParameters
    ) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider ID is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name is required");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required for provider: " + id);
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL is required for provider: " + id);
        }
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("At least one model must be specified for provider: " + id);
        }
        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive for provider: " + id);
        }

        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.models = List.copyOf(models);
        this.weight = weight;
        this.timeout = timeout != null ? timeout : Duration.ofSeconds(30);
        this.rateLimit = rateLimit;
        this.enabled = enabled != null ? enabled : true;
        this.priority = priority != null ? priority : 1;
        this.customParameters = customParameters != null ? Map.copyOf(customParameters) : Map.of();
    }

    public boolean supportsModel(String model) {
        return models.contains(model);
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
