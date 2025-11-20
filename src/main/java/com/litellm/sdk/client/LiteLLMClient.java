package com.litellm.sdk.client;

import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.routing.Router;

import java.util.List;

public record LiteLLMClient(Router router) {
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        return router.routeChatCompletion(router.providers(), request).block();
    }

    public TextCompletionResponse textCompletion(TextCompletionRequest request) {
        return router.routeTextCompletion(router.providers(), request).block();
    }

    public EmbeddingResponse createEmbedding(EmbeddingRequest request) {
        return router.routeEmbedding(router.providers(), request).block();
    }

    public List<ChatCompletionResponse> batchChatCompletion(List<ChatCompletionRequest> requests) {
        return requests.stream()
                .map(this::chatCompletion)
                .toList();
    }

    public List<TextCompletionResponse> batchTextCompletion(List<TextCompletionRequest> requests) {
        return requests.stream()
                .map(this::textCompletion)
                .toList();
    }

    public void close() {
    }
}
