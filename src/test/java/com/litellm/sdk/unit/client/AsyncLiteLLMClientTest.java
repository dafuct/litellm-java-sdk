package com.litellm.sdk.unit.client;

import com.litellm.sdk.config.ClientConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.Message;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice.ResponseMessage;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.client.AsyncLiteLLMClient;
import com.litellm.sdk.routing.Router;
import com.litellm.sdk.retry.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsyncLiteLLMClient Unit Tests")
class AsyncLiteLLMClientTest {

    @Mock
    private Router mockRouter;

    @Mock
    private RetryPolicy mockRetryPolicy;

    @Mock
    private ClientConfig mockConfig;

    private AsyncLiteLLMClient client;

    private ChatCompletionRequest chatRequest;
    private TextCompletionRequest textRequest;
    private EmbeddingRequest embeddingRequest;
    private ChatCompletionResponse chatResponse;
    private TextCompletionResponse textResponse;
    private EmbeddingResponse embeddingResponse;

    @BeforeEach
    void setUp() {
        client = new AsyncLiteLLMClient(mockRouter, mockRetryPolicy, mockConfig);

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

        chatResponse = ChatCompletionResponse.builder()
            .id("chat-123")
            .choices(List.of(
                Choice.builder()
                    .index(0)
                    .finishReason("stop")
                    .message(ResponseMessage.builder()
                        .content("Hello! How can I help you?")
                        .role("assistant")
                        .images(List.of())
                        .thinkingBlocks(List.of())
                        .build())
                    .build()
            ))
            .provider("openai")
            .model("gpt-3.5-turbo")
            .cached(false)
            .timestamp(Instant.now())
            .build();

        textResponse = TextCompletionResponse.builder()
            .id("text-123")
            .content("Hello text response")
            .provider("openai")
            .model("gpt-3.5-turbo")
            .build();

        embeddingResponse = EmbeddingResponse.builder()
            .id("emb-123")
            .provider("openai")
            .model("text-embedding-ada-002")
            .build();

        lenient().when(mockRouter.providers()).thenReturn(List.of());
        lenient().when(mockRouter.routeChatCompletion(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(Mono.empty());
        lenient().when(mockRouter.routeTextCompletion(anyList(), any(TextCompletionRequest.class)))
            .thenReturn(Mono.empty());
        lenient().when(mockRouter.routeEmbedding(anyList(), any(EmbeddingRequest.class)))
            .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should execute async chat completion")
    void shouldExecuteAsyncChatCompletion() {
        // Given
        when(mockRouter.routeChatCompletion(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(Mono.just(chatResponse));

        // When
        Mono<ChatCompletionResponse> result = client.chatCompletion(chatRequest);

        // Then
        assertThat(result).isNotNull();
        ChatCompletionResponse response = result.block();
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("chat-123");
        verify(mockRouter).routeChatCompletion(anyList(), any(ChatCompletionRequest.class));
    }

    @Test
    @DisplayName("Should execute async text completion")
    void shouldExecuteAsyncTextCompletion() {
        // Given
        when(mockRouter.routeTextCompletion(anyList(), any(TextCompletionRequest.class)))
            .thenReturn(Mono.just(textResponse));

        // When
        Mono<TextCompletionResponse> result = client.textCompletion(textRequest);

        // Then
        assertThat(result).isNotNull();
        TextCompletionResponse response = result.block();
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("text-123");
        verify(mockRouter).routeTextCompletion(anyList(), any(TextCompletionRequest.class));
    }

    @Test
    @DisplayName("Should execute async embedding creation")
    void shouldExecuteAsyncEmbedding() {
        // Given
        when(mockRouter.routeEmbedding(anyList(), any(EmbeddingRequest.class)))
            .thenReturn(Mono.just(embeddingResponse));

        // When
        Mono<EmbeddingResponse> result = client.embeddings(embeddingRequest);

        // Then
        assertThat(result).isNotNull();
        EmbeddingResponse response = result.block();
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("emb-123");
        verify(mockRouter).routeEmbedding(anyList(), any(EmbeddingRequest.class));
    }

    @Test
    @DisplayName("Should execute async streaming chat completion")
    void shouldExecuteAsyncStreamingChatCompletion() {
        // Given
        when(mockRouter.routeChatCompletion(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(Mono.just(chatResponse));

        // When
        Mono<ChatCompletionResponse> result = client.chatCompletionStream(chatRequest);

        // Then
        assertThat(result).isNotNull();
        ChatCompletionResponse response = result.block();
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("chat-123");
        verify(mockRouter).routeChatCompletion(anyList(), any(ChatCompletionRequest.class));
    }

    @Test
    @DisplayName("Should return config")
    void shouldReturnConfig() {
        // When
        ClientConfig config = client.getConfig();

        // Then
        assertThat(config).isNotNull();
        assertThat(config).isEqualTo(mockConfig);
    }

    @Test
    @DisplayName("Should return router")
    void shouldReturnRouter() {
        // When
        Router router = client.getRouter();

        // Then
        assertThat(router).isNotNull();
        assertThat(router).isEqualTo(mockRouter);
    }

    @Test
    @DisplayName("Should return retry policy")
    void shouldReturnRetryPolicy() {
        // When
        RetryPolicy retryPolicy = client.getRetryPolicy();

        // Then
        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy).isEqualTo(mockRetryPolicy);
    }

    @Test
    @DisplayName("Should create client with config constructor")
    void shouldCreateClientWithConfigConstructor() {
        // Given
        lenient().when(mockConfig.providers()).thenReturn(List.of());
        lenient().when(mockConfig.routingStrategy()).thenReturn(null);
        lenient().when(mockConfig.retry()).thenReturn(null);

        // When
        AsyncLiteLLMClient newClient = new AsyncLiteLLMClient(mockConfig);

        // Then
        assertThat(newClient).isNotNull();
    }

    @Test
    @DisplayName("Should handle chat completion errors with retry")
    void shouldHandleChatCompletionErrorsWithRetry() {
        // Given
        RuntimeException error = new RuntimeException("Test error");
        when(mockRouter.routeChatCompletion(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(Mono.error(error));

        // When
        Mono<ChatCompletionResponse> result = client.chatCompletion(chatRequest);

        // Then
        assertThat(result).isNotNull();
        assertThatThrownBy(result::block)
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle text completion errors with retry")
    void shouldHandleTextCompletionErrorsWithRetry() {
        // Given
        RuntimeException error = new RuntimeException("Test error");
        when(mockRouter.routeTextCompletion(anyList(), any(TextCompletionRequest.class)))
            .thenReturn(Mono.error(error));

        // When
        Mono<TextCompletionResponse> result = client.textCompletion(textRequest);

        // Then
        assertThat(result).isNotNull();
        assertThatThrownBy(result::block)
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle embedding errors with retry")
    void shouldHandleEmbeddingErrorsWithRetry() {
        // Given
        RuntimeException error = new RuntimeException("Test error");
        when(mockRouter.routeEmbedding(anyList(), any(EmbeddingRequest.class)))
            .thenReturn(Mono.error(error));

        // When
        Mono<EmbeddingResponse> result = client.embeddings(embeddingRequest);

        // Then
        assertThat(result).isNotNull();
        assertThatThrownBy(result::block)
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should return reactive Mono for all operations")
    void shouldReturnReactiveMonoForAllOperations() {
        // When & Then
        assertThat(client.chatCompletion(chatRequest)).isInstanceOf(Mono.class);
        assertThat(client.textCompletion(textRequest)).isInstanceOf(Mono.class);
        assertThat(client.embeddings(embeddingRequest)).isInstanceOf(Mono.class);
        assertThat(client.chatCompletionStream(chatRequest)).isInstanceOf(Mono.class);
    }
}
