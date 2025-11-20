package com.litellm.sdk.integration;

import com.litellm.sdk.client.LiteLLMClient;
import com.litellm.sdk.config.CacheConfig;
import com.litellm.sdk.config.ClientConfig;
import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.config.RetryConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.Message;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.provider.openai.OpenAIProvider;
import com.litellm.sdk.routing.Router;
import com.litellm.sdk.routing.strategy.RoundRobinStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for LiteLLM SDK with real API calls to LiteLLM
 * This test requires actual API keys and makes real HTTP requests
 */
@DisplayName("LiteLLM Client Integration Tests (Real API)")
class LiteLLMClientIntegrationTest {

    private static final String OPENAI_API_KEY = "OPENAI_API_KEY";

    private LiteLLMClient client;

    @BeforeEach
    void setUp() {
        List<ProviderConfig> providerConfigs = new ArrayList<>();

            ProviderConfig openAIConfig = ProviderConfig.builder()
                .id("openai")
                .name("OpenAI")
                .apiKey(OPENAI_API_KEY)
                .baseUrl("https://openai.com/v1")
                .models(List.of("chatgpt-4"))
                .weight(3)
                .timeout(Duration.ofSeconds(30))
                .build();
            providerConfigs.add(openAIConfig);

        ClientConfig config = ClientConfig.builder()
            .providers(providerConfigs)
            .cache(CacheConfig.builder()
                .enabled(true)
                .ttl(Duration.ofMinutes(10))
                .maxSize(100)
                .build())
            .retry(RetryConfig.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofMillis(500))
                .maxDelay(Duration.ofSeconds(5))
                .build())
            .timeout(Duration.ofSeconds(30))
            .build();

        RoundRobinStrategy strategy = new RoundRobinStrategy(config.routingStrategy());

        List<Provider> providers = providerConfigs.stream()
            .map(this::createProviderFromConfig)
            .toList();

        Router router = new Router(config, providers, strategy);

        client = new LiteLLMClient(router);
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    private Provider createProviderFromConfig(ProviderConfig config) {
        if (config.id().equals("openai") || config.baseUrl().contains("openai.com")) {
            return new OpenAIProvider(config);
        } else {
            throw new IllegalArgumentException("Unknown provider type: " + config.id());
        }
    }

    @Test
    @DisplayName("Should make successful chat completion request")
    void shouldMakeSuccessfulChatCompletion() {
        Message message = Message.builder()
            .role(Message.Role.USER)
            .content("""
                        [
                          {
                            "type": "text",
                            "text": "what two color on image?"
                          },
                          {
                            "type": "image_url",
                            "image_url": {
                              "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
                            }
                          }
                        ]""")
            .build();

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("chatgpt-4")
            .messages(List.of(message))
            .build();

        ChatCompletionResponse response = client.chatCompletion(request);

        assertThat(response).isNotNull();
    }
}
