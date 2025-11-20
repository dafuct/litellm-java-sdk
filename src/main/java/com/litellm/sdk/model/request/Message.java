package com.litellm.sdk.model.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record Message(Role role, String content) {
    public Message {
        if (role == null) {
            throw new IllegalArgumentException("Message role is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content is required");
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException("Message content must not exceed 10,000 characters");
        }
    }

    public enum Role {
        SYSTEM, USER, ASSISTANT
    }
}
