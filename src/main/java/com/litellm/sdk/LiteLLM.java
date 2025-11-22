package com.litellm.sdk;

import com.litellm.sdk.config.LiteLLMConfig;
import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.util.EnvironmentValidator;
import com.litellm.sdk.util.KeyValidator;
import com.litellm.sdk.util.ModelValidator;
import java.util.*;

public final class LiteLLM {
    private LiteLLM() {
    }

    public static boolean checkValidKey(String model, String apiKey) {
        return KeyValidator.checkValidKey(model, apiKey);
    }

    public static List<String> getValidModels() {
        return ModelValidator.getValidModels();
    }

    public static List<String> getValidModels(String provider) {
        return ModelValidator.getValidModels(provider);
    }

    public static EnvironmentValidator.EnvironmentValidationResult validateEnvironment(String model) {
        return EnvironmentValidator.validateEnvironment(model);
    }

    public static List<EnvironmentValidator.EnvironmentValidationResult> validateAllEnvironments() {
        return EnvironmentValidator.validateAllEnvironments();
    }
}
