package com.litellm.sdk.util;

import com.litellm.sdk.config.LiteLLMConfig;
import java.util.*;

public final class EnvironmentValidator {
    private EnvironmentValidator() {
    }

    public static EnvironmentValidationResult validateEnvironment(String model) {
        if (model == null || model.trim().isEmpty()) {
            return new EnvironmentValidationResult(
                    "unknown",
                    false,
                    List.of("Model name is required"),
                    Map.of(),
                    "Model name is required"
            );
        }

        String[] parts = model.split("/", 2);
        String provider = parts.length == 2 ? parts[0] : determineProviderFromModel(parts[0]);
        String modelName = parts.length == 2 ? parts[1] : parts[0];

        List<String> missingKeys = new ArrayList<>();
        Map<String, String> configuredKeys = new HashMap<>();

        switch (provider.toLowerCase()) {
            case "openai", "azure":
                missingKeys.addAll(validateOpenAI(configuredKeys));
                break;
            case "anthropic":
                missingKeys.addAll(validateAnthropic(configuredKeys));
                break;
            case "xai":
                missingKeys.addAll(validateXai(configuredKeys));
                break;
            case "replicate":
                missingKeys.addAll(validateReplicate(configuredKeys));
                break;
            case "togetherai", "together":
                missingKeys.addAll(validateTogetherAI(configuredKeys));
                break;
            case "gemini":
                missingKeys.addAll(validateGemini(configuredKeys));
                break;
            case "bedrock":
                missingKeys.addAll(validateBedrock(configuredKeys));
                break;
            case "vertex":
                missingKeys.addAll(validateVertexAI(configuredKeys));
                break;
            case "watson":
                missingKeys.addAll(validateWatson(configuredKeys));
                break;
            default:
                missingKeys.addAll(validateOpenAI(configuredKeys));
        }

        boolean isValid = missingKeys.isEmpty();
        String message = isValid ?
                String.format("Environment is valid for model: %s", model) :
                String.format("Missing required configuration for model %s: %s", model, String.join(", ", missingKeys));

        return new EnvironmentValidationResult(
                provider,
                isValid,
                missingKeys,
                configuredKeys,
                message
        );
    }

    public static List<EnvironmentValidationResult> validateAllEnvironments() {
        List<EnvironmentValidationResult> results = new ArrayList<>();

        if (LiteLLMConfig.getOpenaiKey() != null || LiteLLMConfig.getApiKey() != null) {
            Map<String, String> configured = new HashMap<>();
            List<String> missing = validateOpenAI(configured);
            results.add(new EnvironmentValidationResult(
                    "openai",
                    missing.isEmpty(),
                    missing,
                    configured,
                    missing.isEmpty() ? "OpenAI environment is valid" :
                            "OpenAI: " + String.join(", ", missing)
            ));
        }

        if (LiteLLMConfig.getAnthropicKey() != null) {
            Map<String, String> configured = new HashMap<>();
            List<String> missing = validateAnthropic(configured);
            results.add(new EnvironmentValidationResult(
                    "anthropic",
                    missing.isEmpty(),
                    missing,
                    configured,
                    missing.isEmpty() ? "Anthropic environment is valid" :
                            "Anthropic: " + String.join(", ", missing)
            ));
        }

        if (LiteLLMConfig.getXaiKey() != null) {
            Map<String, String> configured = new HashMap<>();
            List<String> missing = validateXai(configured);
            results.add(new EnvironmentValidationResult(
                    "xai",
                    missing.isEmpty(),
                    missing,
                    configured,
                    missing.isEmpty() ? "XAI environment is valid" :
                            "XAI: " + String.join(", ", missing)
            ));
        }

        if (LiteLLMConfig.getReplicateKey() != null) {
            Map<String, String> configured = new HashMap<>();
            List<String> missing = validateReplicate(configured);
            results.add(new EnvironmentValidationResult(
                    "replicate",
                    missing.isEmpty(),
                    missing,
                    configured,
                    missing.isEmpty() ? "Replicate environment is valid" :
                            "Replicate: " + String.join(", ", missing)
            ));
        }

        if (LiteLLMConfig.getTogetheraiKey() != null) {
            Map<String, String> configured = new HashMap<>();
            List<String> missing = validateTogetherAI(configured);
            results.add(new EnvironmentValidationResult(
                    "togetherai",
                    missing.isEmpty(),
                    missing,
                    configured,
                    missing.isEmpty() ? "Together AI environment is valid" :
                            "Together AI: " + String.join(", ", missing)
            ));
        }

        if (LiteLLMConfig.getAzureApiBase() != null) {
            Map<String, String> configured = new HashMap<>();
            List<String> missing = validateAzure(configured);
            results.add(new EnvironmentValidationResult(
                    "azure",
                    missing.isEmpty(),
                    missing,
                    configured,
                    missing.isEmpty() ? "Azure environment is valid" :
                            "Azure: " + String.join(", ", missing)
            ));
        }

        return results;
    }

    private static List<String> validateOpenAI(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiKey = LiteLLMConfig.getOpenaiKey() != null ?
                LiteLLMConfig.getOpenaiKey() :
                LiteLLMConfig.getApiKey();

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configured.put("api_key", maskKey(apiKey));
        } else {
            missing.add("OPENAI_API_KEY or apiKey");
        }

        String baseUrl = LiteLLMConfig.getOpenaiBaseUrl();
        if (baseUrl != null) {
            configured.put("base_url", baseUrl);
        }

        String apiVersion = LiteLLMConfig.getApiVersion();
        if (apiVersion != null) {
            configured.put("api_version", apiVersion);
        }

        return missing;
    }

    private static List<String> validateAnthropic(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiKey = LiteLLMConfig.getAnthropicKey();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configured.put("api_key", maskKey(apiKey));
        } else {
            missing.add("ANTHROPIC_API_KEY");
        }

        return missing;
    }

    private static List<String> validateXai(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiKey = LiteLLMConfig.getXaiKey();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configured.put("api_key", maskKey(apiKey));
        } else {
            missing.add("XAI_API_KEY");
        }

        return missing;
    }

    private static List<String> validateReplicate(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiKey = LiteLLMConfig.getReplicateKey();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configured.put("api_key", maskKey(apiKey));
        } else {
            missing.add("REPLICATE_API_KEY");
        }

        return missing;
    }

    private static List<String> validateTogetherAI(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiKey = LiteLLMConfig.getTogetheraiKey();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configured.put("api_key", maskKey(apiKey));
        } else {
            missing.add("TOGETHERAI_API_KEY");
        }

        return missing;
    }

    private static List<String> validateGemini(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            configured.put("api_key", maskKey(apiKey));
        } else {
            missing.add("GEMINI_API_KEY");
        }

        return missing;
    }

    private static List<String> validateAzure(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String apiBase = LiteLLMConfig.getAzureApiBase();
        if (apiBase != null && !apiBase.trim().isEmpty()) {
            configured.put("api_base", apiBase);
        } else {
            missing.add("AZURE_API_BASE");
        }

        String apiVersion = LiteLLMConfig.getAzureApiVersion();
        if (apiVersion != null && !apiVersion.trim().isEmpty()) {
            configured.put("api_version", apiVersion);
        } else {
            missing.add("AZURE_API_VERSION");
        }

        String apiType = LiteLLMConfig.getAzureApiType();
        if (apiType != null) {
            configured.put("api_type", apiType);
        }

        return missing;
    }

    private static List<String> validateBedrock(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String region = System.getenv("AWS_REGION");

        if (accessKey != null && !accessKey.trim().isEmpty()) {
            configured.put("access_key", "***");
        } else {
            missing.add("AWS_ACCESS_KEY_ID");
        }

        if (secretKey != null && !secretKey.trim().isEmpty()) {
            configured.put("secret_key", "***");
        } else {
            missing.add("AWS_SECRET_ACCESS_KEY");
        }

        if (region != null && !region.trim().isEmpty()) {
            configured.put("region", region);
        } else {
            missing.add("AWS_REGION");
        }

        return missing;
    }

    private static List<String> validateVertexAI(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        String project = System.getenv("GOOGLE_CLOUD_PROJECT");
        String location = System.getenv("GOOGLE_CLOUD_LOCATION");
        String credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (project != null && !project.trim().isEmpty()) {
            configured.put("project", project);
        } else {
            missing.add("GOOGLE_CLOUD_PROJECT (vertex_project)");
        }

        if (location != null && !location.trim().isEmpty()) {
            configured.put("location", location);
        } else {
            missing.add("GOOGLE_CLOUD_LOCATION (vertex_location)");
        }

        if (credentials != null) {
            configured.put("credentials", credentials);
        } else {
            missing.add("GOOGLE_APPLICATION_CREDENTIALS");
        }

        return missing;
    }

    private static List<String> validateWatson(Map<String, String> configured) {
        List<String> missing = new ArrayList<>();

        LiteLLMConfig.ProviderConfigMap params = LiteLLMConfig.getProviderParameters(LiteLLMConfig.Provider.WATSON);

        if (params != null && params.project() != null) {
            configured.put("project", params.project());
        } else {
            missing.add("watsonx_project");
        }

        if (params != null && params.regionName() != null) {
            configured.put("region", params.regionName());
        } else {
            missing.add("watsonx_region_name");
        }

        if (params != null && params.token() != null) {
            configured.put("token", "***");
        } else {
            missing.add("watsonx_token");
        }

        return missing;
    }

    private static String determineProviderFromModel(String modelName) {
        if (modelName == null) {
            return "openai";
        }

        String lower = modelName.toLowerCase();

        if (lower.contains("claude")) {
            return "anthropic";
        } else if (lower.contains("grok")) {
            return "xai";
        } else if (lower.contains("gemini")) {
            return "gemini";
        }

        return "openai";
    }

    private static String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "***" + key.substring(key.length() - 4);
    }

    public record EnvironmentValidationResult(
            String provider,
            boolean valid,
            List<String> missingKeys,
            Map<String, String> configuredKeys,
            String message
    ) {
        @Override
        public String toString() {
            return String.format(
                    "EnvironmentValidationResult{provider='%s', valid=%s, missing=%s, message='%s'}",
                    provider, valid, missingKeys, message
            );
        }
    }
}
