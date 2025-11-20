package com.litellm.sdk.provider.openai;

import java.util.Map;

public final class OpenAIModels {
    private OpenAIModels() {}

    public static final String GPT_4 = "gpt-4";
    public static final String GPT_4_TURBO = "gpt-4-turbo";
    public static final String GPT_3_5_TURBO = "gpt-3.5-turbo";
    public static final String GPT_3_5_TURBO_16K = "gpt-3.5-turbo-16k";

    public static final Map<String, String> MODEL_ALIASES = Map.of(
        "gpt-4", GPT_4,
        "gpt-4-turbo", GPT_4_TURBO,
        "gpt-3.5-turbo", GPT_3_5_TURBO,
        "gpt-3.5-turbo-16k", GPT_3_5_TURBO_16K
    );

    public static String resolveModel(String model) {
        return MODEL_ALIASES.getOrDefault(model, model);
    }
}
