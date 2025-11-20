package com.litellm.sdk.client;

import com.litellm.sdk.config.ClientConfig;
import com.litellm.sdk.error.LiteLLMException;
import com.litellm.sdk.error.RetryExhaustedException;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.retry.RetryPolicy;
import com.litellm.sdk.routing.Router;
import com.litellm.sdk.routing.strategy.RoundRobinStrategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Getter
public class AsyncLiteLLMClient {
    private final Router router;
    private final RetryPolicy retryPolicy;
    private final ClientConfig config;

    public AsyncLiteLLMClient(ClientConfig config) {
        this.config = config;
        this.retryPolicy = new RetryPolicy(config.retry() != null ? config.retry() : com.litellm.sdk.config.RetryConfig.builder().build());
        List<Provider> providers = config.providers().stream()
                .map(this::createProvider)
                .toList();
        RoundRobinStrategy strategy = new RoundRobinStrategy(config.routingStrategy());
        this.router = new Router(config, providers, strategy);
    }

    public AsyncLiteLLMClient(Router router, RetryPolicy retryPolicy, ClientConfig config) {
        this.router = router;
        this.retryPolicy = retryPolicy;
        this.config = config;
    }

    public Mono<ChatCompletionResponse> chatCompletion(ChatCompletionRequest request) {
        log.debug("Executing async chat completion request");

        return router.routeChatCompletion(router.providers(), request)
                .retryWhen(buildRetryPolicy())
                .onErrorResume(throwable -> {
                    log.error("Chat completion failed after all retries", throwable);
                    return Mono.error(handleRetryExhausted(throwable));
                });
    }

    public Mono<TextCompletionResponse> textCompletion(TextCompletionRequest request) {
        log.debug("Executing async text completion request");

        return router.routeTextCompletion(router.providers(), request)
                .retryWhen(buildRetryPolicy())
                .onErrorResume(throwable -> {
                    log.error("Text completion failed after all retries", throwable);
                    return Mono.error(handleRetryExhausted(throwable));
                });
    }

    public Mono<EmbeddingResponse> embeddings(EmbeddingRequest request) {
        log.debug("Executing async embedding request");

        return router.routeEmbedding(router.providers(), request)
                .retryWhen(buildRetryPolicy())
                .onErrorResume(throwable -> {
                    log.error("Embedding request failed after all retries", throwable);
                    return Mono.error(handleRetryExhausted(throwable));
                });
    }

    public Mono<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest request) {
        log.debug("Executing async streaming chat completion request");

        return router.routeChatCompletion(router.providers(), request)
                .retryWhen(buildRetryPolicy())
                .onErrorResume(throwable -> {
                    log.error("Streaming chat completion failed after all retries", throwable);
                    return Mono.error(handleRetryExhausted(throwable));
                });
    }

    private Retry buildRetryPolicy() {
        var retryConfig = config.retry();
        if (retryConfig == null) {
            return Retry.backoff(0, Duration.ofMillis(1));
        }

        return Retry.backoff(
                        retryConfig.maxAttempts() - 1,
                        retryConfig.initialDelay()
                )
                .maxBackoff(retryConfig.maxDelay())
                .doBeforeRetry(retryBackoff -> {
                    int attempt = (int) retryBackoff.totalRetries() + 1;
                    Duration delay = retryPolicy.calculateDelay(attempt);
                    log.info("Retry attempt {} with delay {}", attempt, delay);
                })
                .filter(retryPolicy::isRetryable)
                .jitter(retryConfig.jitter() ? 0.1 : 0.0);
    }

    private LiteLLMException handleRetryExhausted(Throwable lastError) {
        return new RetryExhaustedException(
                "Request failed after all retry attempts",
                lastError,
                config.retry().maxAttempts(),
                config.retry().maxAttempts(),
                lastError.getMessage()
        );
    }

    private Provider createProvider(com.litellm.sdk.config.ProviderConfig config) {
        String name = config.id();
        return switch (name.toLowerCase()) {
            case "openai" -> new com.litellm.sdk.provider.openai.OpenAIProvider(config);
            case "anthropic" -> new com.litellm.sdk.provider.anthropic.AnthropicProvider(config);
            case "cohere" -> new com.litellm.sdk.provider.cohere.CohereProvider(config);
            default -> throw new IllegalArgumentException("Unknown provider: " + name);
        };
    }
}
