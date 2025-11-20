package com.litellm.sdk.model.common;

import lombok.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Builder(toBuilder = true)
public record ProviderInfo(String id, String name, String version, String baseUrl, Duration averageLatency,
                           Duration p95Latency, Double successRate, Instant lastHealthCheck, String status,
                           Map<String, Object> metadata) {
    public ProviderInfo(
            String id,
            String name,
            String version,
            String baseUrl,
            Duration averageLatency,
            Duration p95Latency,
            Double successRate,
            Instant lastHealthCheck,
            String status,
            Map<String, Object> metadata
    ) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.baseUrl = baseUrl;
        this.averageLatency = averageLatency;
        this.p95Latency = p95Latency;
        this.successRate = successRate;
        this.lastHealthCheck = lastHealthCheck;
        this.status = status != null ? status : "UNKNOWN";
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public boolean isHealthy() {
        return "HEALTHY".equalsIgnoreCase(status);
    }
}
