package com.litellm.sdk.model.response;

import lombok.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import com.litellm.sdk.model.common.Usage;

@Builder(toBuilder = true)
public record EmbeddingResponse(String id, List<Float> embedding, String provider, String model, Usage usage,
                                Duration latency, Duration providerLatency, Boolean cached, Instant timestamp) {
    public EmbeddingResponse(
            String id,
            List<Float> embedding,
            String provider,
            String model,
            Usage usage,
            Duration latency,
            Duration providerLatency,
            Boolean cached,
            Instant timestamp
    ) {
        this.id = id;
        this.embedding = embedding != null ? List.copyOf(embedding) : null;
        this.provider = provider;
        this.model = model;
        this.usage = usage;
        this.latency = latency;
        this.providerLatency = providerLatency;
        this.cached = cached != null ? cached : false;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }
}
