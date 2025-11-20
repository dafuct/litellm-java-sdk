package com.litellm.sdk.provider.anthropic;

import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice.ResponseMessage;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.provider.ProviderMetrics;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record AnthropicProvider(ProviderConfig config) implements Provider {

    @Override
    public long getRequestCount() {
        return 0;
    }

    @Override
    public long getSuccessCount() {
        return 0;
    }

    @Override
    public long getErrorCount() {
        return 0;
    }

    @Override
    public Duration getAverageLatency() {
        return Duration.ZERO;
    }

    @Override
    public Duration getP95Latency() {
        return Duration.ZERO;
    }

    @Override
    public double getSuccessRate() {
        return 1.0;
    }

    @Override
    public long getCacheHitCount() {
        return 0;
    }

    @Override
    public String getName() {
        return "anthropic";
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public Mono<ChatCompletionResponse> chatCompletion(ChatCompletionRequest request) {
        return Mono.fromCallable(() -> {
            String model = AnthropicModels.resolveModel(request.model());

            ResponseMessage message = ResponseMessage.builder()
                .content("Anthropic response for model: " + model)
                .role("assistant")
                .images(List.of())
                .thinkingBlocks(List.of())
                .build();

            Choice choice = Choice.builder()
                .finishReason("stop")
                .index(0)
                .message(message)
                .build();

            return ChatCompletionResponse.builder()
                    .id(request.id())
                    .choices(List.of(choice))
                    .provider(getName())
                    .model(model)
                    .timestamp(Instant.now())
                    .build();
        });
    }

    @Override
    public Mono<TextCompletionResponse> textCompletion(TextCompletionRequest request) {
        return Mono.fromCallable(() -> {
            String model = AnthropicModels.resolveModel(request.model());

            return TextCompletionResponse.builder()
                    .id(request.id())
                    .content("Anthropic text completion for model: " + model)
                    .provider(getName())
                    .model(model)
                    .build();
        });
    }

    @Override
    public Mono<EmbeddingResponse> createEmbedding(EmbeddingRequest request) {
        return Mono.fromCallable(() -> {
            String model = AnthropicModels.resolveModel(request.model());

            return EmbeddingResponse.builder()
                    .id(request.id())
                    .provider(getName())
                    .model(model)
                    .build();
        });
    }

    @Override
    public ProviderMetrics getMetrics() {
        return this;
    }

    @Override
    public void updateHealth(HealthStatus status) {
    }

    @Override
    public HealthStatus getStatus() {
        return HealthStatus.HEALTHY;
    }

    @Override
    public Duration getLastHealthCheck() {
        return Duration.ofSeconds(30);
    }

    @Override
    public String getFailureReason() {
        return null;
    }

    @Override
    public void updateHealth(HealthStatus status, String failureReason) {
    }
}
