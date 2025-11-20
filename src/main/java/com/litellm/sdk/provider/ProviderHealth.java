package com.litellm.sdk.provider;

import java.time.Duration;

public interface ProviderHealth {
    boolean isHealthy();

    HealthStatus getStatus();

    Duration getLastHealthCheck();

    String getFailureReason();

    void updateHealth(HealthStatus status, String failureReason);

    enum HealthStatus {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }
}
