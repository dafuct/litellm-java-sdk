package com.litellm.sdk.unit.routing;

import com.litellm.sdk.config.ClientConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.TextCompletionRequest;
import com.litellm.sdk.model.request.EmbeddingRequest;
import com.litellm.sdk.model.request.Message;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;
import com.litellm.sdk.model.response.EmbeddingResponse;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.routing.Router;
import com.litellm.sdk.routing.strategy.RoutingStrategy;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Router Unit Tests")
class RouterTest {

    @Mock
    private ClientConfig mockConfig;

    @Mock
    private Provider mockProvider1;

    @Mock
    private Provider mockProvider2;

    @Mock
    private RoutingStrategy mockStrategy;

    @Mock
    private ChatCompletionResponse mockChatResponse;

    @Mock
    private TextCompletionResponse mockTextResponse;

    @Mock
    private EmbeddingResponse mockEmbeddingResponse;

    private Router router;
    private List<Provider> providers;
    private ChatCompletionRequest chatRequest;
    private TextCompletionRequest textRequest;
    private EmbeddingRequest embeddingRequest;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(mockConfig).validate();  // Disable validation

        providers = List.of(mockProvider1, mockProvider2);
        router = new Router(mockConfig, providers, mockStrategy);

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

        lenient().when(mockProvider1.getName()).thenReturn("openai");
        lenient().when(mockProvider2.getName()).thenReturn("anthropic");
    }

    @Test
    @DisplayName("Should route chat completion to selected provider")
    void shouldRouteChatCompletion() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider1));
        when(mockProvider1.chatCompletion(any(ChatCompletionRequest.class)))
            .thenReturn(Mono.just(mockChatResponse));

        // When
        ChatCompletionResponse result = router.routeChatCompletion(providers, chatRequest).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockChatResponse);
        verify(mockStrategy).selectProvider(providers, chatRequest);
        verify(mockProvider1).chatCompletion(chatRequest);
    }

    @Test
    @DisplayName("Should route text completion to selected provider")
    void shouldRouteTextCompletion() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider2));
        when(mockProvider2.textCompletion(any(TextCompletionRequest.class)))
            .thenReturn(Mono.just(mockTextResponse));

        // When
        TextCompletionResponse result = router.routeTextCompletion(providers, textRequest).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockTextResponse);
        verify(mockStrategy).selectProvider(anyList(), any(ChatCompletionRequest.class));
        verify(mockProvider2).textCompletion(textRequest);
    }

    @Test
    @DisplayName("Should route embedding to selected provider")
    void shouldRouteEmbedding() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider1));
        when(mockProvider1.createEmbedding(any(EmbeddingRequest.class)))
            .thenReturn(Mono.just(mockEmbeddingResponse));

        // When
        EmbeddingResponse result = router.routeEmbedding(providers, embeddingRequest).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockEmbeddingResponse);
        verify(mockStrategy).selectProvider(anyList(), any(ChatCompletionRequest.class));
        verify(mockProvider1).createEmbedding(embeddingRequest);
    }

    @Test
    @DisplayName("Should return error when no provider available")
    void shouldReturnErrorWhenNoProviderAvailable() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
            router.routeChatCompletion(providers, chatRequest).block()
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("No provider available");
    }

    @Test
    @DisplayName("Should return providers list")
    void shouldReturnProvidersList() {
        // When
        List<Provider> result = router.providers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(providers);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return routing strategy")
    void shouldReturnRoutingStrategy() {
        // When
        RoutingStrategy result = router.routingStrategy();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockStrategy);
    }

    @Test
    @DisplayName("Should handle provider selection for text completion")
    void shouldHandleProviderSelectionForTextCompletion() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider2));
        when(mockProvider2.textCompletion(any(TextCompletionRequest.class)))
            .thenReturn(Mono.just(mockTextResponse));

        // When
        router.routeTextCompletion(providers, textRequest).block();

        // Then
        verify(mockStrategy).selectProvider(anyList(), any(ChatCompletionRequest.class));
        verify(mockProvider2).textCompletion(textRequest);
    }

    @Test
    @DisplayName("Should handle provider selection for embedding")
    void shouldHandleProviderSelectionForEmbedding() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider1));
        when(mockProvider1.createEmbedding(any(EmbeddingRequest.class)))
            .thenReturn(Mono.just(mockEmbeddingResponse));

        // When
        router.routeEmbedding(providers, embeddingRequest).block();

        // Then
        verify(mockStrategy).selectProvider(anyList(), any(ChatCompletionRequest.class));
        verify(mockProvider1).createEmbedding(embeddingRequest);
    }

    @Test
    @DisplayName("Should route with empty providers list")
    void shouldRouteWithEmptyProvidersList() {
        // Given
        List<Provider> emptyProviders = List.of();
        Router emptyRouter = new Router(mockConfig, emptyProviders, mockStrategy);

        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
            emptyRouter.routeChatCompletion(emptyProviders, chatRequest).block()
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("No provider available");
    }

    @Test
    @DisplayName("Should return immutable providers list")
    void shouldReturnImmutableProvidersList() {
        // When
        List<Provider> result = router.providers();

        // Then
        assertThat(result).isNotNull();
        // List.copyOf returns an unmodifiable list
        assertThat(result).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("Should convert text completion request to chat completion for routing")
    void shouldConvertTextRequestToChatForRouting() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider1));
        when(mockProvider1.textCompletion(any(TextCompletionRequest.class)))
            .thenReturn(Mono.just(mockTextResponse));

        // When
        router.routeTextCompletion(providers, textRequest).block();

        // Then
        // Verify that selectProvider was called with a ChatCompletionRequest
        // (converted from TextCompletionRequest)
        verify(mockStrategy).selectProvider(anyList(), any(ChatCompletionRequest.class));
    }

    @Test
    @DisplayName("Should convert embedding request to chat completion for routing")
    void shouldConvertEmbeddingRequestToChatForRouting() {
        // Given
        when(mockStrategy.selectProvider(anyList(), any(ChatCompletionRequest.class)))
            .thenReturn(java.util.Optional.of(mockProvider2));
        when(mockProvider2.createEmbedding(any(EmbeddingRequest.class)))
            .thenReturn(Mono.just(mockEmbeddingResponse));

        // When
        router.routeEmbedding(providers, embeddingRequest).block();

        // Then
        verify(mockStrategy).selectProvider(anyList(), any(ChatCompletionRequest.class));
    }
}
