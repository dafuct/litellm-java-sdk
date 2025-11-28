package com.litellm.sdk.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModelRegistry {
    private static final Map<String, ModelInfo> MODELS = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static boolean initialized = false;

    private ModelRegistry() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try (InputStream is = ModelRegistry.class.getClassLoader()
                .getResourceAsStream("model_prices.json")) {
            if (is == null) {
                System.err.println("model_prices.json not found in resources");
                initialized = true;
                return;
            }

            JsonNode root = MAPPER.readTree(is);
            JsonNode spec = root.get("sample_spec");

            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String modelName = entry.getKey();

                if ("sample_spec".equals(modelName)) {
                    continue;
                }

                JsonNode modelNode = entry.getValue();
                ModelInfo modelInfo = parseModelInfo(modelName, modelNode, spec);
                if (modelInfo != null) {
                    MODELS.put(modelName, modelInfo);
                }
            }

            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize ModelRegistry: " + e.getMessage());
            initialized = true;
        }
    }

    private static ModelInfo parseModelInfo(String modelName, JsonNode modelNode, JsonNode spec) {
        String provider = getString(modelNode, "litellm_provider", spec, "litellm_provider");
        String mode = getString(modelNode, "mode", spec, "mode");

        double inputCostPerToken = getDouble(modelNode, "input_cost_per_token", spec, "input_cost_per_token");
        double outputCostPerToken = getDouble(modelNode, "output_cost_per_token", spec, "output_cost_per_token");
        double inputCostPerPixel = getDouble(modelNode, "input_cost_per_pixel", spec, "input_cost_per_pixel");
        double outputCostPerPixel = getDouble(modelNode, "output_cost_per_pixel", spec, "output_cost_per_pixel");
        double outputCostPerImage = getDouble(modelNode, "output_cost_per_image", spec, "output_cost_per_image");
        double inputCostPerAudioToken = getDouble(modelNode, "input_cost_per_audio_token", spec, "input_cost_per_audio_token");
        double outputCostPerReasoningToken = getDouble(modelNode, "output_cost_per_reasoning_token", spec, "output_cost_per_reasoning_token");

        int maxInputTokens = getInt(modelNode, "max_input_tokens", spec, "max_input_tokens");
        int maxOutputTokens = getInt(modelNode, "max_output_tokens", spec, "max_output_tokens");
        int maxTokens = getInt(modelNode, "max_tokens", spec, "max_tokens");

        boolean supportsFunctionCalling = getBoolean(modelNode, "supports_function_calling", spec, "supports_function_calling");
        boolean supportsVision = getBoolean(modelNode, "supports_vision", spec, "supports_vision");
        boolean supportsAudioInput = getBoolean(modelNode, "supports_audio_input", spec, "supports_audio_input");
        boolean supportsAudioOutput = getBoolean(modelNode, "supports_audio_output", spec, "supports_audio_output");
        boolean supportsSystemMessages = getBoolean(modelNode, "supports_system_messages", spec, "supports_system_messages");
        boolean supportsPromptCaching = getBoolean(modelNode, "supports_prompt_caching", spec, "supports_prompt_caching");

        return new ModelInfo(
            modelName,
            provider,
            mode,
            inputCostPerToken,
            outputCostPerToken,
            maxInputTokens,
            maxOutputTokens,
            maxTokens,
            supportsFunctionCalling,
            supportsVision,
            supportsAudioInput,
            supportsAudioOutput,
            supportsSystemMessages,
            supportsPromptCaching,
            inputCostPerPixel,
            outputCostPerPixel,
            outputCostPerImage,
            inputCostPerAudioToken,
            outputCostPerReasoningToken
        );
    }

    private static String getString(JsonNode modelNode, String field, JsonNode spec, String specField) {
        JsonNode node = modelNode.get(field);
        if (node != null && !node.isNull()) {
            return node.asText();
        }
        JsonNode specNode = spec.get(specField);
        return specNode != null ? specNode.asText() : "";
    }

    private static double getDouble(JsonNode modelNode, String field, JsonNode spec, String specField) {
        JsonNode node = modelNode.get(field);
        if (node != null && !node.isNull()) {
            return node.asDouble();
        }
        JsonNode specNode = spec.get(specField);
        return specNode != null ? specNode.asDouble() : 0.0;
    }

    private static int getInt(JsonNode modelNode, String field, JsonNode spec, String specField) {
        JsonNode node = modelNode.get(field);
        if (node != null && !node.isNull()) {
            return node.asInt();
        }
        JsonNode specNode = spec.get(specField);
        if (specNode != null) {
            String value = specNode.asText();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static boolean getBoolean(JsonNode modelNode, String field, JsonNode spec, String specField) {
        JsonNode node = modelNode.get(field);
        if (node != null && !node.isNull()) {
            return node.asBoolean();
        }
        JsonNode specNode = spec.get(specField);
        return specNode != null ? specNode.asBoolean() : false;
    }

    public static ModelInfo getModelInfo(String modelName) {
        if (!initialized) {
            initialize();
        }
        return MODELS.get(modelName);
    }

    public static boolean hasModel(String modelName) {
        if (!initialized) {
            initialize();
        }
        return MODELS.containsKey(modelName);
    }

    public static Set<String> getAllModels() {
        if (!initialized) {
            initialize();
        }
        return Collections.unmodifiableSet(MODELS.keySet());
    }

    public static List<ModelInfo> getModelsByProvider(String provider) {
        if (!initialized) {
            initialize();
        }
        return MODELS.values().stream()
            .filter(m -> provider.equals(m.provider()))
            .toList();
    }

    public static List<ModelInfo> getModelsByMode(String mode) {
        if (!initialized) {
            initialize();
        }
        return MODELS.values().stream()
            .filter(m -> mode.equals(m.mode()))
            .toList();
    }

    public static void registerModel(String modelName, ModelInfo modelInfo) {
        if (!initialized) {
            initialize();
        }
        MODELS.put(modelName, modelInfo);
    }

    public static void clear() {
        MODELS.clear();
        initialized = false;
    }
}
