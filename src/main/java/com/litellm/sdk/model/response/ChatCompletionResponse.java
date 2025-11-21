package com.litellm.sdk.model.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

import com.litellm.sdk.model.common.Usage;

@Builder(toBuilder = true)
public record ChatCompletionResponse(String id, Long created, String object, String model, String provider,
                                     List<Choice> choices, Usage usage, Boolean cached, Instant timestamp) {
    @Builder(toBuilder = true)
    public record Choice(String finishReason, Integer index, ResponseMessage message) {
        @Builder(toBuilder = true)
        public record ResponseMessage(String content, String role, List<Object> images, List<Object> thinkingBlocks) {
        }
    }

    // Helper method to get the main content from the first choice
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            Choice firstChoice = choices.get(0);
            if (firstChoice.message() != null) {
                return firstChoice.message().content();
            }
        }
        return null;
    }

    // Helper method to get finish reason
    public String getFinishReason() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).finishReason();
        }
        return null;
    }

    // Helper method to get index
    public Integer getIndex() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).index();
        }
        return null;
    }
}
