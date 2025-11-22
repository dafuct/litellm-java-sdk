package com.litellm.sdk.util;

import com.litellm.sdk.config.LiteLLMConfig;
import com.litellm.sdk.config.ProviderConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class KeyValidator {
    private static final Pattern OPENAI_KEY_PATTERN = Pattern.compile("^sk-[a-zA-Z0-9]{32,}$");
    private static final Pattern ANTHROPIC_KEY_PATTERN = Pattern.compile("^sk-ant-[a-zA-Z0-9-]{32,}$");
    private static final Pattern XAI_KEY_PATTERN = Pattern.compile("^xai-[a-zA-Z0-9]{32,}$");
    private static final Pattern REPLICATE_KEY_PATTERN = Pattern.compile("^[a-z0-9]{32,}$");
    private static final Pattern TOGETHER_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9]{32,}$");

    private KeyValidator() {
    }

    public static boolean checkValidKey(String model, String apiKey) {
        return checkValidKey(model, apiKey, Duration.ofSeconds(5));
    }

    public static boolean checkValidKey(String model, String apiKey, Duration timeout) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        if (!isValidFormat(model, apiKey)) {
            return false;
        }

        try {
            return validateWithApiCall(model, apiKey, timeout);
        } catch (Exception e) {
            return false;
        }
    }

    public static List<KeyValidationResult> validateAllKeys() {
        List<KeyValidationResult> results = new ArrayList<>();

        String openaiKey = LiteLLMConfig.getOpenaiKey();
        if (openaiKey != null) {
            results.add(new KeyValidationResult(
                    "openai",
                    openaiKey,
                    checkValidKey("gpt-3.5-turbo", openaiKey),
                    "openai"
            ));
        }

        String anthropicKey = LiteLLMConfig.getAnthropicKey();
        if (anthropicKey != null) {
            results.add(new KeyValidationResult(
                    "anthropic",
                    anthropicKey,
                    checkValidKey("claude-2", anthropicKey),
                    "anthropic"
            ));
        }

        String xaiKey = LiteLLMConfig.getXaiKey();
        if (xaiKey != null) {
            results.add(new KeyValidationResult(
                    "xai",
                    xaiKey,
                    checkValidKey("grok-beta", xaiKey),
                    "xai"
            ));
        }

        String replicateKey = LiteLLMConfig.getReplicateKey();
        if (replicateKey != null) {
            results.add(new KeyValidationResult(
                    "replicate",
                    replicateKey,
                    checkValidKey("meta/llama-2-70b", replicateKey),
                    "replicate"
            ));
        }

        String togetherKey = LiteLLMConfig.getTogetheraiKey();
        if (togetherKey != null) {
            results.add(new KeyValidationResult(
                    "togetherai",
                    togetherKey,
                    checkValidKey("togethercomputer/RedPajama-INCITE-Chat-3B-v1", togetherKey),
                    "togetherai"
            ));
        }

        return results;
    }

    public static boolean checkValidKey(ProviderConfig config) {
        if (config == null || config.apiKey() == null) {
            return false;
        }
        return checkValidKey(getDefaultModel(config), config.apiKey());
    }

    private static boolean isValidFormat(String model, String apiKey) {
        if (model == null || apiKey == null) {
            return false;
        }

        String modelLower = model.toLowerCase();

        if (modelLower.contains("gpt") || modelLower.contains("dalle") || modelLower.contains("whisper")) {
            return OPENAI_KEY_PATTERN.matcher(apiKey).matches();
        } else if (modelLower.contains("claude")) {
            return ANTHROPIC_KEY_PATTERN.matcher(apiKey).matches();
        } else if (modelLower.contains("grok")) {
            return XAI_KEY_PATTERN.matcher(apiKey).matches();
        } else if (modelLower.contains("replicate")) {
            return REPLICATE_KEY_PATTERN.matcher(apiKey).matches();
        } else if (modelLower.contains("together")) {
            return TOGETHER_KEY_PATTERN.matcher(apiKey).matches();
        }

        return OPENAI_KEY_PATTERN.matcher(apiKey).matches() || ANTHROPIC_KEY_PATTERN.matcher(apiKey).matches();
    }

    private static boolean validateWithApiCall(String model, String apiKey, Duration timeout) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();

        String provider = determineProvider(model);
        String url = buildValidationUrl(provider);
        String body = buildValidationRequestBody(model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    private static String determineProvider(String model) {
        if (model == null) {
            return "openai";
        }

        String modelLower = model.toLowerCase();

        if (modelLower.contains("claude")) {
            return "anthropic";
        } else if (modelLower.contains("grok")) {
            return "xai";
        } else if (modelLower.contains("replicate")) {
            return "replicate";
        } else if (modelLower.contains("together")) {
            return "togetherai";
        }

        return "openai";
    }

    private static String buildValidationUrl(String provider) {
        return switch (provider.toLowerCase()) {
            case "anthropic" -> "https://api.anthropic.com/v1/messages";
            case "xai" -> "https://api.x.ai/v1/chat/completions";
            case "replicate" -> "https://api.replicate.com/v1/predictions";
            case "togetherai" -> "https://api.together.xyz/v1/chat/completions";
            default -> "https://api.openai.com/v1/chat/completions";
        };
    }

    private static String buildValidationRequestBody(String model) {
        return String.format("""
            {
                "model": "%s",
                "messages": [
                    {
                        "role": "user",
                        "content": "Hi"
                    }
                ],
                "max_tokens": 1
            }
            """, model);
    }

    private static String getDefaultModel(ProviderConfig config) {
        if (config.models() != null && !config.models().isEmpty()) {
            return config.models().get(0);
        }
        return "gpt-3.5-turbo";
    }

    public record KeyValidationResult(String provider, String apiKey, boolean valid, String type) {
        public String getMaskedKey() {
            if (apiKey == null || apiKey.length() < 8) {
                return "***";
            }
            return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
        }

        @Override
        public String toString() {
            return String.format("KeyValidationResult{provider='%s', key='%s', valid=%s, type='%s'}",
                    provider, getMaskedKey(), valid, type);
        }
    }
}
