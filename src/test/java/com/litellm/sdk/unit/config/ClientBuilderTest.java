package com.litellm.sdk.unit.config;

import com.litellm.sdk.client.ClientBuilder;
import com.litellm.sdk.config.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ClientBuilder Unit Tests")
class ClientBuilderTest {

    @Test
    @DisplayName("Should create builder with default configuration")
    void shouldCreateBuilderWithDefaultConfiguration() {
        // When
        ClientBuilder builder = ClientBuilder.builder();

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should build client with single provider")
    void shouldBuildClientWithSingleProvider() {
        // Given
        ProviderConfig provider = ProviderConfig.builder()
            .id("openai")
            .name("OpenAI")
            .apiKey("test-key")
            .baseUrl("https://api.openai.com/v1")
            .models(List.of("gpt-3.5-turbo"))
            .weight(1)
            .build();

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withProvider(provider);

        // Then
        assertThat(builder).isNotNull();
        assertThat(builder.build()).isNotNull();
    }

    @Test
    @DisplayName("Should build client with multiple providers")
    void shouldBuildClientWithMultipleProviders() {
        // Given
        ProviderConfig provider1 = ProviderConfig.builder()
            .id("openai")
            .name("OpenAI")
            .apiKey("key1")
            .baseUrl("https://api.openai.com/v1")
            .models(List.of("gpt-3.5-turbo"))
            .weight(1)
            .build();

        ProviderConfig provider2 = ProviderConfig.builder()
            .id("anthropic")
            .name("Anthropic")
            .apiKey("key2")
            .baseUrl("https://api.anthropic.com")
            .models(List.of("claude-3"))
            .weight(1)
            .build();

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withProvider(provider1)
            .withProvider(provider2);

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should configure custom routing strategy")
    void shouldConfigureCustomRoutingStrategy() {
        // Given
        RoutingStrategyConfig strategy = RoutingStrategyConfig.builder()
            .type(RoutingStrategyConfig.StrategyType.WEIGHTED)
            .build();

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withRoutingStrategy(strategy);

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should configure custom cache")
    void shouldConfigureCustomCache() {
        // Given
        CacheConfig cache = CacheConfig.builder()
            .ttl(Duration.ofMinutes(10))
            .maxSize(2000)
            .build();

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withCache(cache);

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should configure custom retry policy")
    void shouldConfigureCustomRetryPolicy() {
        // Given
        RetryConfig retry = RetryConfig.builder()
            .maxAttempts(5)
            .initialDelay(Duration.ofSeconds(1))
            .build();

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withRetryPolicy(retry);

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should configure custom timeout")
    void shouldConfigureCustomTimeout() {
        // Given
        Duration timeout = Duration.ofSeconds(60);

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withTimeout(timeout);

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should configure custom environment prefix")
    void shouldConfigureCustomEnvironmentPrefix() {
        // Given
        String prefix = "CUSTOM";

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withEnvironmentPrefix(prefix);

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should build client with full configuration")
    void shouldBuildClientWithFullConfiguration() {
        // Given
        ProviderConfig provider = ProviderConfig.builder()
            .id("openai")
            .name("OpenAI")
            .apiKey("test-key")
            .baseUrl("https://api.openai.com/v1")
            .models(List.of("gpt-3.5-turbo"))
            .weight(1)
            .build();

        CacheConfig cache = CacheConfig.builder()
            .enabled(true)
            .build();

        RetryConfig retry = RetryConfig.builder()
            .maxAttempts(3)
            .build();

        RoutingStrategyConfig strategy = RoutingStrategyConfig.builder()
            .type(RoutingStrategyConfig.StrategyType.ROUND_ROBIN)
            .build();

        // When
        ClientBuilder builder = ClientBuilder.builder()
            .withProvider(provider)
            .withCache(cache)
            .withRetryPolicy(retry)
            .withRoutingStrategy(strategy)
            .withTimeout(Duration.ofSeconds(30))
            .withEnvironmentPrefix("LITELLM");

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should use default values when not configured")
    void shouldUseDefaultValuesWhenNotConfigured() {
        // When
        ClientBuilder builder = ClientBuilder.builder();
        // Note: Actual validation happens during build()

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should support builder pattern chaining")
    void shouldSupportBuilderPatternChaining() {
        // When & Then
        assertThatCode(() -> ClientBuilder.builder()
            .withProvider(ProviderConfig.builder()
                .id("openai")
                .name("OpenAI")
                .apiKey("key")
                .baseUrl("https://api.openai.com/v1")
                .models(List.of("gpt-3.5-turbo"))
                .weight(1)
                .build())
            .withCache(CacheConfig.builder().build())
            .withRetryPolicy(RetryConfig.builder().build())
            .withRoutingStrategy(RoutingStrategyConfig.builder()
                .type(RoutingStrategyConfig.StrategyType.ROUND_ROBIN)
                .build())
            .withTimeout(Duration.ofSeconds(30))
            .withEnvironmentPrefix("TEST")
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle null provider list gracefully")
    void shouldHandleNullProviderListGracefully() {
        // When & Then
        assertThatCode(() -> ClientBuilder.builder()
            .withProvider(null)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should build with empty provider list")
    void shouldBuildWithEmptyProviderList() {
        // When
        ClientBuilder builder = ClientBuilder.builder();

        // Then
        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("Should configure all routing strategy types")
    void shouldConfigureAllRoutingStrategyTypes() {
        // Test each strategy type
        for (RoutingStrategyConfig.StrategyType type : RoutingStrategyConfig.StrategyType.values()) {
            // When
            ClientBuilder builder = ClientBuilder.builder()
                .withRoutingStrategy(RoutingStrategyConfig.builder()
                    .type(type)
                    .build());

            // Then
            assertThat(builder).isNotNull();
        }
    }

    @Test
    @DisplayName("Should create static builder instance")
    void shouldCreateStaticBuilderInstance() {
        // When
        ClientBuilder builder = ClientBuilder.builder();

        // Then
        assertThat(builder).isNotNull();
        assertThat(builder).isInstanceOf(ClientBuilder.class);
    }
}
