package com.litellm.sdk.provider.anthropic;

import java.util.Map;

public final class AnthropicModels {
    private AnthropicModels() {}

    public static final String CLAUDE_3_OPUS = "claude-3-opus-20240229";
    public static final String CLAUDE_3_SONNET = "claude-3-sonnet-20240229";
    public static final String CLAUDE_3_HAIKU = "claude-3-haiku-20240307";

    public static final Map<String, String> MODEL_ALIASES = Map.of(
        "claude-3-opus", CLAUDE_3_OPUS,
        "claude-3-sonnet", CLAUDE_3_SONNET,
        "claude-3-haiku", CLAUDE_3_HAIKU
    );

    public static String resolveModel(String model) {
        return MODEL_ALIASES.getOrDefault(model, model);
    }
}
