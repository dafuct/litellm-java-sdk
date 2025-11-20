package com.litellm.sdk.routing;

import com.litellm.sdk.config.ClientConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.routing.strategy.RoutingStrategy;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Router(ClientConfig config, List<Provider> providers, RoutingStrategy routingStrategy) {

    public Mono<Provider> routeRequest(List<Provider> providers, ChatCompletionRequest request) {
        return Mono.fromCallable(() -> routingStrategy.selectProvider(providers, request))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .switchIfEmpty(Mono.error(new RuntimeException("No provider available")));
    }

    public Mono<ChatCompletionResponse> routeChatCompletion(List<Provider> providers, ChatCompletionRequest request) {
        return routeRequest(providers, request)
                .flatMap(provider -> provider.chatCompletion(request));
    }

    public Mono<TextCompletionResponse> routeTextCompletion(List<Provider> providers, TextCompletionRequest request) {
        return Mono.fromCallable(() -> {
                    ChatCompletionRequest chatReq = ChatCompletionRequest.builder()
                            .model(request.model())
                            .messages(List.of(
                                com.litellm.sdk.model.request.Message.builder()
                                    .role(com.litellm.sdk.model.request.Message.Role.SYSTEM)
                                    .content("Text completion routing")
                                    .build()
                            ))
                            .build();
                    return routingStrategy.selectProvider(providers, chatReq);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(provider -> provider.textCompletion(request));
    }

    public Mono<EmbeddingResponse> routeEmbedding(List<Provider> providers, EmbeddingRequest request) {
        return Mono.fromCallable(() -> {
                    ChatCompletionRequest chatReq = ChatCompletionRequest.builder()
                            .model(request.model())
                            .messages(List.of(
                                com.litellm.sdk.model.request.Message.builder()
                                    .role(com.litellm.sdk.model.request.Message.Role.SYSTEM)
                                    .content("Embedding routing")
                                    .build()
                            ))
                            .build();
                    return routingStrategy.selectProvider(providers, chatReq);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(provider -> provider.createEmbedding(request));
    }

    @Override
    public List<Provider> providers() {
        return List.copyOf(providers);
    }
}
