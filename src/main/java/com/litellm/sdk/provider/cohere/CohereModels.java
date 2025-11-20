package com.litellm.sdk.provider.cohere;

import java.util.Map;

public final class CohereModels {
    private CohereModels() {}

    public static final String COMMAND = "command";
    public static final String COMMAND_LIGHT = "command-light";
    public static final String COMMAND_R = "command-r";
    public static final String COMMAND_R_PLUS = "command-r-plus";

    public static final Map<String, String> MODEL_ALIASES = Map.of(
        "command", COMMAND,
        "command-light", COMMAND_LIGHT,
        "command-r", COMMAND_R,
        "command-r-plus", COMMAND_R_PLUS
    );

    public static String resolveModel(String model) {
        return MODEL_ALIASES.getOrDefault(model, model);
    }
}
