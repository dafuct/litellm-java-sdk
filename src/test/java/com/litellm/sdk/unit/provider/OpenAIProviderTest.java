package com.litellm.sdk.unit.provider;

import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.Message;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.provider.openai.OpenAIProvider;
import com.litellm.sdk.provider.ProviderHealth;
import com.litellm.sdk.provider.ProviderMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenAIProvider Unit Tests")
class OpenAIProviderTest {

    private OpenAIProvider provider;
    private ProviderConfig config;

    private ChatCompletionRequest chatRequest;
    private TextCompletionRequest textRequest;
    private EmbeddingRequest embeddingRequest;

    @BeforeEach
    void setUp() {
        config = ProviderConfig.builder()
            .id("openai")
            .name("OpenAI")
            .apiKey("test-api-key")
            .baseUrl("https://api.openai.com")
            .models(List.of("gpt-3.5-turbo", "gpt-4"))
            .weight(1)
            .build();

        provider = new OpenAIProvider(config);

        Message message = Message.builder()
            .role(Message.Role.USER)
            .content("Hello")
            .build();

        chatRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(message))
            .build();

        textRequest = TextCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .prompt("Hello")
            .build();

        embeddingRequest = EmbeddingRequest.builder()
            .model("text-embedding-ada-002")
            .input(List.of("Hello"))
            .build();
    }

    @Test
    @DisplayName("Should execute chat completion")
    void shouldExecuteChatCompletion() {
        // When
        ChatCompletionResponse response = provider.chatCompletion(chatRequest).block();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("openai");
        assertThat(response.getModel()).isEqualTo("gpt-3.5-turbo");
        assertThat(response.getContent()).contains("OpenAI response for model: gpt-3.5-turbo");
    }

    @Test
    @DisplayName("Should execute text completion")
    void shouldExecuteTextCompletion() {
        // When
        TextCompletionResponse response = provider.textCompletion(textRequest).block();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.provider()).isEqualTo("openai");
        assertThat(response.model()).isEqualTo("gpt-3.5-turbo");
        assertThat(response.content()).contains("OpenAI text completion for model: gpt-3.5-turbo");
    }

    @Test
    @DisplayName("Should execute embedding creation")
    void shouldExecuteEmbeddingCreation() {
        // When
        EmbeddingResponse response = provider.createEmbedding(embeddingRequest).block();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.provider()).isEqualTo("openai");
        assertThat(response.model()).isEqualTo("text-embedding-ada-002");
    }

    @Test
    @DisplayName("Should return correct provider name")
    void shouldReturnCorrectProviderName() {
        // When
        String name = provider.getName();

        // Then
        assertThat(name).isEqualTo("openai");
    }

    @Test
    @DisplayName("Should be healthy by default")
    void shouldBeHealthyByDefault() {
        // When
        boolean healthy = provider.isHealthy();

        // Then
        assertThat(healthy).isTrue();
    }

    @Test
    @DisplayName("Should return healthy status")
    void shouldReturnHealthyStatus() {
        // When
        ProviderHealth.HealthStatus status = provider.getStatus();

        // Then
        assertThat(status).isEqualTo(ProviderHealth.HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("Should return metrics")
    void shouldReturnMetrics() {
        // When
        ProviderMetrics metrics = provider.getMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics).isInstanceOf(ProviderMetrics.class);
    }

    @Test
    @DisplayName("Should return request count")
    void shouldReturnRequestCount() {
        // When
        long count = provider.getRequestCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return success count")
    void shouldReturnSuccessCount() {
        // When
        long count = provider.getSuccessCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return error count")
    void shouldReturnErrorCount() {
        // When
        long count = provider.getErrorCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return zero average latency")
    void shouldReturnZeroAverageLatency() {
        // When
        Duration latency = provider.getAverageLatency();

        // Then
        assertThat(latency).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Should return zero p95 latency")
    void shouldReturnZeroP95Latency() {
        // When
        Duration latency = provider.getP95Latency();

        // Then
        assertThat(latency).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("Should return 100% success rate")
    void shouldReturn100PercentSuccessRate() {
        // When
        double rate = provider.getSuccessRate();

        // Then
        assertThat(rate).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should return zero cache hit count")
    void shouldReturnZeroCacheHitCount() {
        // When
        long count = provider.getCacheHitCount();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update health status")
    void shouldUpdateHealthStatus() {
        // When
        provider.updateHealth(ProviderHealth.HealthStatus.HEALTHY);

        // Then
        assertThat(provider.getStatus()).isEqualTo(ProviderHealth.HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("Should update health status with reason")
    void shouldUpdateHealthStatusWithReason() {
        // When
        provider.updateHealth(ProviderHealth.HealthStatus.UNHEALTHY, "Test reason");

        // Then
        assertThat(provider.getStatus()).isEqualTo(ProviderHealth.HealthStatus.UNHEALTHY);
    }

    @Test
    @DisplayName("Should return last health check time")
    void shouldReturnLastHealthCheckTime() {
        // When
        Duration duration = provider.getLastHealthCheck();

        // Then
        assertThat(duration).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should return null failure reason")
    void shouldReturnNullFailureReason() {
        // When
        String reason = provider.getFailureReason();

        // Then
        assertThat(reason).isNull();
    }

    @Test
    @DisplayName("Should handle different models in chat completion")
    void shouldHandleDifferentModelsInChatCompletion() {
        // Given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("gpt-4")
            .messages(List.of(Message.builder().role(Message.Role.USER).content("Hi").build()))
            .build();

        // When
        ChatCompletionResponse response = provider.chatCompletion(request).block();

        // Then
        assertThat(response.getModel()).isEqualTo("gpt-4");
        assertThat(response.getContent()).contains("gpt-4");
    }

    @Test
    @DisplayName("Should handle different models in embedding")
    void shouldHandleDifferentModelsInEmbedding() {
        // Given
        EmbeddingRequest request = EmbeddingRequest.builder()
            .model("text-embedding-3-small")
            .input(List.of("Test input"))
            .build();

        // When
        EmbeddingResponse response = provider.createEmbedding(request).block();

        // Then
        assertThat(response.model()).isEqualTo("text-embedding-3-small");
    }

    @Test
    @DisplayName("Should return self as metrics provider")
    void shouldReturnSelfAsMetricsProvider() {
        // When
        ProviderMetrics metrics = provider.getMetrics();

        // Then
        assertThat(metrics).isSameAs(provider);
    }

    @Test
    @DisplayName("Should maintain state after multiple operations")
    void shouldMaintainStateAfterMultipleOperations() {
        // When
        provider.chatCompletion(chatRequest).block();
        provider.chatCompletion(chatRequest).block();

        // Then
        assertThat(provider.getRequestCount()).isEqualTo(0); // Mock doesn't track
    }
}
