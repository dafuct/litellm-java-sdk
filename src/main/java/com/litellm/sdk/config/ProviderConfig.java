package com.litellm.sdk.config;

import lombok.Builder;
import lombok.With;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Builder
@With
public record ProviderConfig(
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
    // Compact constructor for validation
    public ProviderConfig {
        // Create defensive copies
        models = models != null ? List.copyOf(models) : null;
        customParameters = customParameters != null ? Map.copyOf(customParameters) : null;

        // Only validate if all required fields are non-null (for builder pattern)
        if (id != null && name != null && apiKey != null && baseUrl != null && models != null && weight != null) {
            validate(id, name, apiKey, baseUrl, models, weight);
        }
    }

    /**
     * Creates and validates a ProviderConfig instance.
     * This is the recommended way to create a ProviderConfig.
     */
    public static ProviderConfig of(
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
        return new ProviderConfig(
                id,
                name,
                apiKey,
                baseUrl,
                models,
                weight,
                timeout,
                rateLimit,
                enabled,
                priority,
                customParameters
        );
    }

    private static void validate(String id, String name, String apiKey, String baseUrl, List<String> models, Integer weight) {
        requireNonBlank(id, "Provider ID is required");
        requireNonBlank(name, "Provider name is required");
        requireNonBlank(apiKey, "API key is required for provider: " + id);
        requireNonBlank(baseUrl, "Base URL is required for provider: " + id);
        requireNonEmpty(models, "At least one model must be specified for provider: " + id);
        requirePositive(weight, "Weight must be positive for provider: " + id);
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireNonEmpty(List<?> value, String message) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public boolean supportsModel(String model) {
        return models.contains(model);
    }

    public boolean isEnabled() {
        return enabled != null ? enabled : true;
    }

    /**
     * Accessor for the enabled field.
     * Returns the enabled value, or true if null.
     */
    public Boolean enabled() {
        return enabled != null ? enabled : true;
    }
}
