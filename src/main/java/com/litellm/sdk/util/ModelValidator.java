package com.litellm.sdk.util;

import com.litellm.sdk.config.LiteLLMConfig;
import java.util.*;

public final class ModelValidator {
    private static final Map<String, List<String>> OPENAI_MODELS = Map.of(
            "openai", List.of(
                    "gpt-4", "gpt-4-turbo", "gpt-4o", "gpt-4o-mini",
                    "gpt-3.5-turbo", "gpt-3.5-turbo-16k",
                    "dall-e-3", "dall-e-2",
                    "whisper-1", "tts-1"
            )
    );

    private static final Map<String, List<String>> ANTHROPIC_MODELS = Map.of(
            "anthropic", List.of(
                    "claude-3-opus", "claude-3-sonnet", "claude-3-haiku",
                    "claude-2.1", "claude-2", "claude-instant-1.2"
            )
    );

    private static final Map<String, List<String>> FIREWORKS_MODELS = Map.of(
            "fireworks", List.of(
                    "accounts/fireworks/models/llama-v2-7b-chat",
                    "accounts/fireworks/models/llama-v2-13b-chat",
                    "accounts/fireworks/models/llama-v2-70b-chat"
            )
    );

    private static final Map<String, List<String>> LITELLM_PROXY_MODELS = Map.of(
            "litellm_proxy", List.of(
                    "gpt-3.5-turbo", "gpt-4", "claude-2", "claude-instant",
                    "j2-ultra", "j2-mid", "command-nightly"
            )
    );

    private static final Map<String, List<String>> GEMINI_MODELS = Map.of(
            "gemini", List.of(
                    "gemini-pro", "gemini-pro-vision"
            )
    );

    private static final Map<String, List<String>> XAI_MODELS = Map.of(
            "xai", List.of(
                    "grok-beta", "grok-vision-beta"
            )
    );

    private ModelValidator() {
    }

    public static List<String> getValidModels() {
        return getValidModels(false);
    }

    public static List<String> getValidModels(boolean checkProviderEndpoint) {
        List<String> allModels = new ArrayList<>();

        if (LiteLLMConfig.getOpenaiKey() != null) {
            allModels.addAll(OPENAI_MODELS.get("openai"));
        }

        if (LiteLLMConfig.getAnthropicKey() != null) {
            allModels.addAll(ANTHROPIC_MODELS.get("anthropic"));
        }

        if (LiteLLMConfig.getXaiKey() != null) {
            allModels.addAll(XAI_MODELS.get("xai"));
        }

        if (LiteLLMConfig.getTogetheraiKey() != null) {
            allModels.addAll(FIREWORKS_MODELS.get("fireworks"));
        }

        if (LiteLLMConfig.getReplicateKey() != null) {
            allModels.addAll(List.of(
                    "meta/llama-2-70b", "meta/codellama-70b",
                    "stability-ai/stable-diffusion-xl-base-1.0"
            ));
        }

        if (LiteLLMConfig.getApiBase() != null) {
            allModels.addAll(LITELLM_PROXY_MODELS.get("litellm_proxy"));
        }

        if (System.getenv("GEMINI_API_KEY") != null) {
            allModels.addAll(GEMINI_MODELS.get("gemini"));
        }

        if (checkProviderEndpoint) {
        }

        return allModels.stream()
                .distinct()
                .sorted()
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public static List<String> getValidModels(String provider) {
        if (provider == null) {
            return getValidModels();
        }

        return switch (provider.toLowerCase()) {
            case "openai" -> OPENAI_MODELS.get("openai");
            case "anthropic" -> ANTHROPIC_MODELS.get("anthropic");
            case "fireworks" -> FIREWORKS_MODELS.get("fireworks");
            case "litellm", "proxy" -> LITELLM_PROXY_MODELS.get("litellm_proxy");
            case "gemini" -> GEMINI_MODELS.get("gemini");
            case "xai" -> XAI_MODELS.get("xai");
            case "replicate" -> List.of(
                    "meta/llama-2-70b", "meta/codellama-70b",
                    "stability-ai/stable-diffusion-xl-base-1.0"
            );
            case "togetherai" -> List.of(
                    "togethercomputer/RedPajama-INCITE-Chat-3B-v1",
                    "togethercomputer/RedPajama-INCITE-7B-Chat",
                    "meta/llama-2-7b", "meta/llama-2-13b", "meta/llama-2-70b"
            );
            default -> getValidModels();
        };
    }

    public static List<String> getValidModels(String provider, boolean checkProviderEndpoint) {
        List<String> models = getValidModels(provider);

        if (checkProviderEndpoint) {
        }

        return models != null ? models : List.of();
    }

    public static boolean isValidModel(String model) {
        if (model == null) {
            return false;
        }

        List<String> validModels = getValidModels();
        return validModels.contains(model);
    }

    public static Map<String, List<String>> getModelsByProvider() {
        Map<String, List<String>> grouped = new LinkedHashMap<>();

        if (LiteLLMConfig.getOpenaiKey() != null) {
            grouped.put("openai", OPENAI_MODELS.get("openai"));
        }

        if (LiteLLMConfig.getAnthropicKey() != null) {
            grouped.put("anthropic", ANTHROPIC_MODELS.get("anthropic"));
        }

        if (LiteLLMConfig.getXaiKey() != null) {
            grouped.put("xai", XAI_MODELS.get("xai"));
        }

        if (LiteLLMConfig.getTogetheraiKey() != null) {
            grouped.put("togetherai", List.of(
                    "togethercomputer/RedPajama-INCITE-Chat-3B-v1",
                    "togethercomputer/RedPajama-INCITE-7B-Chat",
                    "meta/llama-2-7b", "meta/llama-2-13b", "meta/llama-2-70b"
            ));
        }

        if (LiteLLMConfig.getReplicateKey() != null) {
            grouped.put("replicate", List.of(
                    "meta/llama-2-70b", "meta/codellama-70b",
                    "stability-ai/stable-diffusion-xl-base-1.0"
            ));
        }

        if (System.getenv("GEMINI_API_KEY") != null) {
            grouped.put("gemini", GEMINI_MODELS.get("gemini"));
        }

        return grouped;
    }

    public static String getProviderForModel(String model) {
        if (model == null) {
            return "unknown";
        }

        Map<String, List<String>> modelsByProvider = getModelsByProvider();

        for (Map.Entry<String, List<String>> entry : modelsByProvider.entrySet()) {
            if (entry.getValue().contains(model)) {
                return entry.getKey();
            }
        }

        return "unknown";
    }
}
