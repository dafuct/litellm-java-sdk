package com.litellm.sdk.unit.client;

import com.litellm.sdk.client.LiteLLMClient;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.Message;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice.ResponseMessage;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.routing.Router;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LiteLLMClient Unit Tests")
class LiteLLMClientTest {

    @Mock
    private Router mockRouter;

    @Mock
    private Provider mockProvider;

    private LiteLLMClient client;

    private ChatCompletionRequest chatRequest;
    private TextCompletionRequest textRequest;
    private EmbeddingRequest embeddingRequest;
    private ChatCompletionResponse chatResponse;
    private TextCompletionResponse textResponse;
    private EmbeddingResponse embeddingResponse;

    @BeforeEach
    void setUp() {
        client = new LiteLLMClient(mockRouter);

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

        lenient().when(mockRouter.providers()).thenReturn(List.of(mockProvider));
    }

    @Test
    @DisplayName("Should execute chat completion successfully")
    void shouldExecuteChatCompletion() {
        // Given
        when(mockRouter.routeChatCompletion(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(Mono.just(chatResponse));

        // When
        ChatCompletionResponse result = client.chatCompletion(chatRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("chat-123");
        assertThat(result.getContent()).isEqualTo("Hello! How can I help you?");
        verify(mockRouter).routeChatCompletion(anyList(), any(ChatCompletionRequest.class));
    }

    @Test
    @DisplayName("Should execute text completion successfully")
    void shouldExecuteTextCompletion() {
        // Given
        when(mockRouter.routeTextCompletion(anyList(), any(TextCompletionRequest.class)))
            .thenReturn(Mono.just(textResponse));

        // When
        TextCompletionResponse result = client.textCompletion(textRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("text-123");
        assertThat(result.content()).isEqualTo("Hello text response");
        verify(mockRouter).routeTextCompletion(anyList(), any(TextCompletionRequest.class));
    }

    @Test
    @DisplayName("Should execute embedding creation successfully")
    void shouldExecuteEmbedding() {
        // Given
        when(mockRouter.routeEmbedding(anyList(), any(EmbeddingRequest.class)))
            .thenReturn(Mono.just(embeddingResponse));

        // When
        EmbeddingResponse result = client.createEmbedding(embeddingRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("emb-123");
        assertThat(result.model()).isEqualTo("text-embedding-ada-002");
        verify(mockRouter).routeEmbedding(anyList(), any(EmbeddingRequest.class));
    }

    @Test
    @DisplayName("Should execute batch chat completion")
    void shouldExecuteBatchChatCompletion() {
        // Given
        ChatCompletionRequest request2 = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(Message.builder().role(Message.Role.USER).content("Hi").build()))
            .build();

        when(mockRouter.routeChatCompletion(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(Mono.just(chatResponse));

        List<ChatCompletionRequest> requests = List.of(chatRequest, request2);

        // When
        List<ChatCompletionResponse> results = client.batchChatCompletion(requests);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(chatResponse);
        assertThat(results.get(1)).isEqualTo(chatResponse);
    }

    @Test
    @DisplayName("Should execute batch text completion")
    void shouldExecuteBatchTextCompletion() {
        // Given
        TextCompletionRequest request2 = TextCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .prompt("Hi")
            .build();

        when(mockRouter.routeTextCompletion(anyList(), any(TextCompletionRequest.class)))
            .thenReturn(Mono.just(textResponse));

        List<TextCompletionRequest> requests = List.of(textRequest, request2);

        // When
        List<TextCompletionResponse> results = client.batchTextCompletion(requests);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(textResponse);
        assertThat(results.get(1)).isEqualTo(textResponse);
    }

    @Test
    @DisplayName("Should close client without errors")
    void shouldCloseClient() {
        // When & Then
        assertThatCode(() -> client.close()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle empty batch requests")
    void shouldHandleEmptyBatch() {
        // When
        List<ChatCompletionResponse> results = client.batchChatCompletion(List.of());

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should return router instance")
    void shouldReturnRouter() {
        // When
        Router router = client.router();

        // Then
        assertThat(router).isEqualTo(mockRouter);
    }
}
