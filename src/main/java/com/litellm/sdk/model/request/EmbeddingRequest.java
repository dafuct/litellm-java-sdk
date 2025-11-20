package com.litellm.sdk.model.request;

import lombok.Builder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder(toBuilder = true)
public record EmbeddingRequest(String id, String model, List<String> input, String inputText, Set<String> providerHints,
                               Duration timeout, Map<String, Object> metadata) {
    public EmbeddingRequest(
            String id,
            String model,
            List<String> input,
            String inputText,
            Set<String> providerHints,
            Duration timeout,
            Map<String, Object> metadata
    ) {
        this.id = id != null ? id : generateId();
        this.model = model;
        this.input = input != null ? List.copyOf(input) : null;
        this.inputText = inputText;
        this.providerHints = providerHints != null ? Set.copyOf(providerHints) : Set.of();
        this.timeout = timeout;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();

        validate();
    }

    private void validate() {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model is required");
        }

        boolean hasInput = (input != null && !input.isEmpty()) || (inputText != null && !inputText.trim().isEmpty());
        if (!hasInput) {
            throw new IllegalArgumentException("Either input list or input text must be provided");
        }

        if (input != null && inputText != null) {
            throw new IllegalArgumentException("Cannot specify both input list and input text");
        }
    }

    private static String generateId() {
        return "req_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }
}
