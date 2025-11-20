package com.litellm.sdk.model.response;

import lombok.Builder;

import java.time.Duration;
import java.time.Instant;
import com.litellm.sdk.model.common.Usage;

@Builder(toBuilder = true)
public record TextCompletionResponse(String id, String content, String provider, String model, Usage usage,
                                     Duration latency, Duration providerLatency, FinishReason finishReason,
                                     Boolean cached, Instant timestamp) {
    public TextCompletionResponse(
            String id,
            String content,
            String provider,
            String model,
            Usage usage,
            Duration latency,
            Duration providerLatency,
            FinishReason finishReason,
            Boolean cached,
            Instant timestamp
    ) {
        this.id = id;
        this.content = content;
        this.provider = provider;
        this.model = model;
        this.usage = usage;
        this.latency = latency;
        this.providerLatency = providerLatency;
        this.finishReason = finishReason;
        this.cached = cached != null ? cached : false;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public enum FinishReason {
        LENGTH,
        STOP,
        CONTENT_FILTER,
        ERROR
    }
}
