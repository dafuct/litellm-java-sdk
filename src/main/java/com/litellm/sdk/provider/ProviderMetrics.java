package com.litellm.sdk.provider;

import java.time.Duration;

public interface ProviderMetrics {
    long getRequestCount();

    long getSuccessCount();

    long getErrorCount();

    Duration getAverageLatency();

    Duration getP95Latency();

    double getSuccessRate();

    long getCacheHitCount();

    default void recordRequest(Duration latency, boolean success) {}
}
