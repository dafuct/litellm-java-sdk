package com.litellm.sdk.config;

import lombok.Builder;

import java.time.Duration;
import java.util.Map;

@Builder(toBuilder = true)
public record RoutingStrategyConfig(StrategyType type, Map<String, Object> parameters, StrategyType fallbackStrategy,
                                    Duration healthCheckInterval) {
    public RoutingStrategyConfig(
            StrategyType type,
            Map<String, Object> parameters,
            StrategyType fallbackStrategy,
            Duration healthCheckInterval
    ) {
        if (type == null) {
            throw new IllegalArgumentException("Routing strategy type is required");
        }
        this.type = type;
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.fallbackStrategy = fallbackStrategy;
        this.healthCheckInterval = healthCheckInterval != null ? healthCheckInterval : Duration.ofSeconds(30);
    }

    public enum StrategyType {
        ROUND_ROBIN,
        WEIGHTED,
        LATENCY_BASED,
        COST_OPTIMIZED,
        FAILOVER
    }
}
