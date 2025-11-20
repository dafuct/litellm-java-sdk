package com.litellm.sdk.model.request;

import lombok.Builder;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Map;

@Builder(toBuilder = true)
public record ChatCompletionRequest(String id, List<Message> messages, String model, Double temperature,
                                    Integer maxTokens, Double topP, Boolean stream, Set<String> providerHints,
                                    Duration timeout, Map<String, Object> metadata) {
    public ChatCompletionRequest(
            String id,
            List<Message> messages,
            String model,
            Double temperature,
            Integer maxTokens,
            Double topP,
            Boolean stream,
            Set<String> providerHints,
            Duration timeout,
            Map<String, Object> metadata
    ) {
        this.id = id != null ? id : generateId();
        this.messages = messages;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.stream = stream != null ? stream : false;
        this.providerHints = providerHints != null ? Set.copyOf(providerHints) : Set.of();
        this.timeout = timeout;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();

        validate();
    }

    private void validate() {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be empty");
        }
        if (messages.size() > 100) {
            throw new IllegalArgumentException("Maximum 100 messages allowed per request");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
            throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
        }
        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
        if (topP != null && (topP < 0.0 || topP > 1.0)) {
            throw new IllegalArgumentException("Top-p must be between 0.0 and 1.0");
        }
    }

    private static String generateId() {
        return "req_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }
}
