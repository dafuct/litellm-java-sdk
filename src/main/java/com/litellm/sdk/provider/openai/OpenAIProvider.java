package com.litellm.sdk.provider.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.model.common.Usage;
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
import com.litellm.sdk.provider.ProviderHealth;
import com.litellm.sdk.provider.ProviderMetrics;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
public class OpenAIProvider implements Provider {
    private final ProviderConfig config;
    private volatile HealthStatus healthStatus = HealthStatus.HEALTHY;
    private volatile String failureReason;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIProvider(ProviderConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public long getRequestCount() {
        return 0;
    }

    @Override
    public long getSuccessCount() {
        return 0;
    }

    @Override
    public long getErrorCount() {
        return 0;
    }

    @Override
    public Duration getAverageLatency() {
        return Duration.ZERO;
    }

    @Override
    public Duration getP95Latency() {
        return Duration.ZERO;
    }

    @Override
    public double getSuccessRate() {
        return 1.0;
    }

    @Override
    public long getCacheHitCount() {
        return 0;
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public boolean isHealthy() {
        return healthStatus == HealthStatus.HEALTHY;
    }

    @Override
    public Mono<ChatCompletionResponse> chatCompletion(ChatCompletionRequest request) {
        return Mono.fromCallable(() -> {
            try {
                // Build request body
                String requestBody = buildChatCompletionRequest(request);

                // Create HTTP request
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.baseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(config.timeout() != null ? config.timeout().getSeconds() : 30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                // Send HTTP request
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Parse successful response
                    ChatCompletionResponse chatResponse = parseChatCompletionResponse(response.body());
                    updateHealth(HealthStatus.HEALTHY, null);
                    return chatResponse;
                } else {
                    // Handle error
                    String errorMsg = "HTTP " + response.statusCode() + ": " + response.body();
                    updateHealth(HealthStatus.UNHEALTHY, errorMsg);
                    throw new RuntimeException(errorMsg);
                }

            } catch (Exception e) {
                updateHealth(HealthStatus.UNHEALTHY, e.getMessage());
                throw new RuntimeException("Failed to call OpenAI API", e);
            }
        })
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
        .onErrorResume(throwable -> {
            return Mono.error(new RuntimeException("Chat completion failed: " + throwable.getMessage(), throwable));
        });
    }

    private String buildChatCompletionRequest(ChatCompletionRequest request) {
        try {
            // Build request using ObjectMapper for proper JSON serialization
            var requestMap = new java.util.HashMap<String, Object>();
            requestMap.put("model", request.model());

            // Build messages array
            var messagesArray = new java.util.ArrayList<java.util.Map<String, Object>>();
            for (Message msg : request.messages()) {
                var messageMap = new java.util.HashMap<String, Object>();
                // Convert role to lowercase for API compatibility
                messageMap.put("role", msg.role().toString().toLowerCase());

                // Parse content - it could be a simple string or a JSON array string
                String content = msg.content();
                if (content != null) {
                    // Try to parse as JSON array first (for multimodal content)
                    try {
                        Object contentObj = objectMapper.readValue(content, Object.class);
                        messageMap.put("content", contentObj);
                    } catch (Exception e) {
                        // If not JSON, treat as simple string
                        messageMap.put("content", content);
                    }
                }

                messagesArray.add(messageMap);
            }
            requestMap.put("messages", messagesArray);

            // Add optional parameters
            if (request.maxTokens() != null) {
                requestMap.put("max_tokens", request.maxTokens());
            }
            if (request.temperature() != null) {
                requestMap.put("temperature", request.temperature());
            }

            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request JSON", e);
        }
    }

    private ChatCompletionResponse parseChatCompletionResponse(String responseBody) throws Exception {
        JsonNode jsonResponse = objectMapper.readTree(responseBody);

        String id = jsonResponse.has("id") ? jsonResponse.get("id").asText() : null;
        Long created = jsonResponse.has("created") ? jsonResponse.get("created").asLong() : null;
        String object = jsonResponse.has("object") ? jsonResponse.get("object").asText() : "chat.completion";
        String model = jsonResponse.has("model") ? jsonResponse.get("model").asText() : null;

        // Parse choices array
        List<Choice> choices = new ArrayList<>();
        JsonNode choicesNode = jsonResponse.get("choices");
        if (choicesNode != null && choicesNode.isArray()) {
            for (JsonNode choiceNode : choicesNode) {
                String finishReason = choiceNode.has("finish_reason") ? choiceNode.get("finish_reason").asText() : null;
                Integer index = choiceNode.has("index") ? choiceNode.get("index").asInt() : 0;

                // Parse message object
                ResponseMessage message = null;
                JsonNode messageNode = choiceNode.get("message");
                if (messageNode != null) {
                    String content = messageNode.has("content") ? messageNode.get("content").asText() : null;
                    String role = messageNode.has("role") ? messageNode.get("role").asText() : null;

                    // Parse images and thinking_blocks arrays
                    List<Object> images = new ArrayList<>();
                    List<Object> thinkingBlocks = new ArrayList<>();

                    if (messageNode.has("images") && messageNode.get("images").isArray()) {
                        for (JsonNode img : messageNode.get("images")) {
                            images.add(objectMapper.convertValue(img, Object.class));
                        }
                    }

                    if (messageNode.has("thinking_blocks") && messageNode.get("thinking_blocks").isArray()) {
                        for (JsonNode block : messageNode.get("thinking_blocks")) {
                            thinkingBlocks.add(objectMapper.convertValue(block, Object.class));
                        }
                    }

                    message = ResponseMessage.builder()
                        .content(content)
                        .role(role)
                        .images(images)
                        .thinkingBlocks(thinkingBlocks)
                        .build();
                }

                choices.add(Choice.builder()
                    .finishReason(finishReason)
                    .index(index)
                    .message(message)
                    .build());
            }
        }

        // Parse usage
        Usage usage = null;
        JsonNode usageNode = jsonResponse.get("usage");
        if (usageNode != null) {
            usage = objectMapper.convertValue(usageNode, Usage.class);
        }

        // Build and return response
        return ChatCompletionResponse.builder()
            .id(id)
            .created(created)
            .object(object)
            .model(model)
            .provider(getName())
            .choices(choices)
            .usage(usage)
            .cached(false)
            .timestamp(Instant.now())
            .build();
    }

    @Override
    public Mono<TextCompletionResponse> textCompletion(TextCompletionRequest request) {
        return Mono.fromCallable(() -> {
            String model = OpenAIModels.resolveModel(request.model());

            // Convert to chat completion format
            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .model(request.model())
                .messages(List.of(
                    Message.builder()
                        .role(Message.Role.SYSTEM)
                        .content("Text completion")
                        .build(),
                    Message.builder()
                        .role(Message.Role.USER)
                        .content(request.prompt())
                        .build()
                ))
                .maxTokens(request.maxTokens())
                .temperature(request.temperature())
                .build();

            // Call chat completion and transform response
            ChatCompletionResponse chatResponse = chatCompletion(chatRequest).block();

            return TextCompletionResponse.builder()
                .id(chatResponse.getId())
                .content(chatResponse.getContent())
                .provider(getName())
                .model(model)
                .build();
        });
    }

    @Override
    public Mono<EmbeddingResponse> createEmbedding(EmbeddingRequest request) {
        return Mono.fromCallable(() -> {
            String model = OpenAIModels.resolveModel(request.model());

            return EmbeddingResponse.builder()
                .id(request.id())
                .provider(getName())
                .model(model)
                .build();
        });
    }

    @Override
    public ProviderMetrics getMetrics() {
        return this;
    }

    @Override
    public void updateHealth(HealthStatus status) {
        this.healthStatus = status;
    }

    @Override
    public HealthStatus getStatus() {
        return healthStatus;
    }

    @Override
    public Duration getLastHealthCheck() {
        return Duration.ofSeconds(30);
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public void updateHealth(HealthStatus status, String failureReason) {
        this.healthStatus = status;
        this.failureReason = failureReason;
    }
}
