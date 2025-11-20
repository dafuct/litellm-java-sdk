package com.litellm.sdk.provider;

import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import reactor.core.publisher.Mono;

public interface Provider extends ProviderHealth, ProviderMetrics {
    String getName();

    boolean isHealthy();

    Mono<ChatCompletionResponse> chatCompletion(ChatCompletionRequest request);

    Mono<TextCompletionResponse> textCompletion(TextCompletionRequest request);

    Mono<EmbeddingResponse> createEmbedding(EmbeddingRequest request);

    ProviderMetrics getMetrics();

    void updateHealth(HealthStatus status);

    default void close() {}
}
