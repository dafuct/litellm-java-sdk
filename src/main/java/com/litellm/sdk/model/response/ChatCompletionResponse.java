package com.litellm.sdk.model.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import com.litellm.sdk.model.common.Usage;

@Value
@Builder(toBuilder = true)
public class ChatCompletionResponse {
    private String id;
    private Long created;
    private String object;
    private String model;
    private String provider;
    private java.util.List<Choice> choices;
    private Usage usage;
    private Boolean cached;
    private Instant timestamp;

    @Value
    @Builder(toBuilder = true)
    public static class Choice {
        private String finishReason;
        private Integer index;
        private ResponseMessage message;

        @Value
        @Builder(toBuilder = true)
        public static class ResponseMessage {
            private String content;
            private String role;
            private java.util.List<Object> images;
            private java.util.List<Object> thinkingBlocks;
        }
    }

    // Helper method to get the main content from the first choice
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            Choice firstChoice = choices.get(0);
            if (firstChoice.getMessage() != null) {
                return firstChoice.getMessage().getContent();
            }
        }
        return null;
    }

    // Helper method to get finish reason
    public String getFinishReason() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getFinishReason();
        }
        return null;
    }

    // Helper method to get index
    public Integer getIndex() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getIndex();
        }
        return null;
    }
}
