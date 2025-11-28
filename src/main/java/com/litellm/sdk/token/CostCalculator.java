package com.litellm.sdk.token;

import com.litellm.sdk.model.ModelInfo;
import com.litellm.sdk.model.ModelRegistry;
import java.util.*;

public class CostCalculator {
    private CostCalculator() {
    }

    public static double costPerToken(String model, int promptTokens, int completionTokens) {
        ModelInfo modelInfo = ModelRegistry.getModelInfo(model);
        if (modelInfo == null) {
            return 0.0;
        }

        double promptCost = modelInfo.inputCostPerToken() * promptTokens;
        double completionCost = modelInfo.outputCostPerToken() * completionTokens;
        return promptCost + completionCost;
    }

    public static double completionCost(String model, int promptTokens, int completionTokens) {
        return costPerToken(model, promptTokens, completionTokens);
    }

    public static double calculateCost(String model, int promptTokens, int completionTokens) {
        return costPerToken(model, promptTokens, completionTokens);
    }

    public static double calculateCost(String model, String prompt, String completion) {
        int promptTokens = Tokenizer.tokenCounter(model, prompt);
        int completionTokens = Tokenizer.tokenCounter(model, completion);
        return costPerToken(model, promptTokens, completionTokens);
    }

    public static int getMaxTokens(String model) {
        ModelInfo modelInfo = ModelRegistry.getModelInfo(model);
        if (modelInfo == null) {
            return 0;
        }
        return modelInfo.maxTokens();
    }

    public static boolean hasPricing(String model) {
        ModelInfo modelInfo = ModelRegistry.getModelInfo(model);
        return modelInfo != null && modelInfo.hasPricing();
    }

    public static Map<String, Double> getModelCost() {
        Map<String, Double> costMap = new HashMap<>();
        Set<String> models = ModelRegistry.getAllModels();

        for (String model : models) {
            ModelInfo modelInfo = ModelRegistry.getModelInfo(model);
            if (modelInfo != null && modelInfo.hasPricing()) {
                costMap.put(model, modelInfo.inputCostPerToken());
            }
        }

        return costMap;
    }

    public static void registerModelPricing(String model, double inputCost, double outputCost) {
        ModelInfo existingModel = ModelRegistry.getModelInfo(model);
        if (existingModel != null) {
            ModelInfo newModelInfo = new ModelInfo(
                existingModel.model(),
                existingModel.provider(),
                existingModel.mode(),
                inputCost,
                outputCost,
                existingModel.maxInputTokens(),
                existingModel.maxOutputTokens(),
                existingModel.maxTokens(),
                existingModel.supportsFunctionCalling(),
                existingModel.supportsVision(),
                existingModel.supportsAudioInput(),
                existingModel.supportsAudioOutput(),
                existingModel.supportsSystemMessages(),
                existingModel.supportsPromptCaching(),
                existingModel.inputCostPerPixel(),
                existingModel.outputCostPerPixel(),
                existingModel.outputCostPerImage(),
                existingModel.inputCostPerAudioToken(),
                existingModel.outputCostPerReasoningToken()
            );
            ModelRegistry.registerModel(model, newModelInfo);
        } else {
            ModelInfo newModelInfo = new ModelInfo(
                model,
                "custom",
                "chat",
                inputCost,
                outputCost,
                0,
                0,
                0,
                false,
                false,
                false,
                false,
                false,
                false,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0
            );
            ModelRegistry.registerModel(model, newModelInfo);
        }
    }

    public static Set<String> getRegisteredModels() {
        return ModelRegistry.getAllModels();
    }
}
