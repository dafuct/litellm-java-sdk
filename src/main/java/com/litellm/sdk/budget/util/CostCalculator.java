package com.litellm.sdk.budget.util;

import com.litellm.sdk.model.budget.CostInfo;
import com.litellm.sdk.model.common.Usage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CostCalculator {
    private static final Logger logger = Logger.getLogger(CostCalculator.class.getName());

    private static final Map<String, ModelPricing> MODEL_PRICINGS = new ConcurrentHashMap<>();

    static {
        MODEL_PRICINGS.put("gpt-4", new ModelPricing(0.03, 0.06));
        MODEL_PRICINGS.put("gpt-4-1106-preview", new ModelPricing(0.01, 0.03));
        MODEL_PRICINGS.put("gpt-4-0125-preview", new ModelPricing(0.01, 0.03));
        MODEL_PRICINGS.put("gpt-4-turbo", new ModelPricing(0.01, 0.03));
        MODEL_PRICINGS.put("gpt-3.5-turbo", new ModelPricing(0.0005, 0.0015));
        MODEL_PRICINGS.put("gpt-3.5-turbo-16k", new ModelPricing(0.003, 0.004));

        MODEL_PRICINGS.put("claude-3-opus", new ModelPricing(0.015, 0.075));
        MODEL_PRICINGS.put("claude-3-sonnet", new ModelPricing(0.003, 0.015));
        MODEL_PRICINGS.put("claude-3-haiku", new ModelPricing(0.00025, 0.00125));
        MODEL_PRICINGS.put("claude-2.1", new ModelPricing(0.008, 0.024));
        MODEL_PRICINGS.put("claude-2", new ModelPricing(0.008, 0.024));
        MODEL_PRICINGS.put("claude-instant", new ModelPricing(0.001, 0.0025));

        MODEL_PRICINGS.put("command", new ModelPricing(0.0015, 0.006));
        MODEL_PRICINGS.put("command-nightly", new ModelPricing(0.0015, 0.006));

        MODEL_PRICINGS.put("j2-ultra", new ModelPricing(0.012, 0.012));
        MODEL_PRICINGS.put("j2-mid", new ModelPricing(0.0012, 0.0012));
        MODEL_PRICINGS.put("j2-light", new ModelPricing(0.0003, 0.0003));

        MODEL_PRICINGS.put("text-bison", new ModelPricing(0.0005, 0.0015));
        MODEL_PRICINGS.put("code-bison", new ModelPricing(0.0005, 0.0015));
    }

    private record ModelPricing(double inputCostPerToken, double outputCostPerToken) {}

    private CostCalculator() {
    }

    public static void registerModelPricing(String model, double inputCostPerToken, double outputCostPerToken) {
        if (model != null && !model.trim().isEmpty()) {
            MODEL_PRICINGS.put(model, new ModelPricing(inputCostPerToken, outputCostPerToken));
            logger.info("Registered custom pricing for model: " + model);
        }
    }

    public static CostInfo calculateCost(String model, Usage usage) {
        return calculateCost(model, usage.getPromptTokens(), usage.getCompletionTokens());
    }

    public static CostInfo calculateCost(String model, Integer promptTokens, Integer completionTokens) {
        if (model == null || model.trim().isEmpty()) {
            return CostInfo.empty(model);
        }

        ModelPricing pricing = MODEL_PRICINGS.get(model);
        if (pricing == null) {
            logger.warning("No pricing information found for model: " + model + ". Cost will be 0.");
            return CostInfo.empty(model);
        }

        int inputTokens = promptTokens != null ? promptTokens : 0;
        int outputTokens = completionTokens != null ? completionTokens : 0;

        double inputCost = (inputTokens / 1000.0) * pricing.inputCostPerToken;
        double outputCost = (outputTokens / 1000.0) * pricing.outputCostPerToken;
        double totalCost = inputCost + outputCost;

        double costPerToken = (inputTokens + outputTokens) > 0 ? totalCost / (inputTokens + outputTokens) : 0.0;

        return new CostInfo(
            totalCost,
            model,
            promptTokens,
            completionTokens,
            inputTokens + outputTokens,
            costPerToken
        );
    }

    public static CostInfo estimateCostFromText(String model, String inputText, String outputText) {
        if (model == null || model.trim().isEmpty()) {
            return CostInfo.empty(model);
        }

        int inputTokens = inputText != null ? (inputText.length() + 3) / 4 : 0;
        int outputTokens = outputText != null ? (outputText.length() + 3) / 4 : 0;

        return calculateCost(model, inputTokens, outputTokens);
    }

    public static CostInfo estimateCostFromMessages(String model, java.util.List<?> messages) {
        if (model == null || model.trim().isEmpty() || messages == null) {
            return CostInfo.empty(model);
        }

        int estimatedTokens = messages.size() * 10;

        for (Object msg : messages) {
            String content = msg.toString();
            estimatedTokens += (content.length() + 3) / 4;
        }

        return calculateCost(model, estimatedTokens, estimatedTokens / 2);
    }

    public static boolean hasPricing(String model) {
        return MODEL_PRICINGS.containsKey(model);
    }

    public static ModelPricing getPricing(String model) {
        return MODEL_PRICINGS.get(model);
    }

    public static ModelPricingInfo getPricingInfo(String model) {
        ModelPricing pricing = MODEL_PRICINGS.get(model);
        if (pricing == null) {
            return null;
        }
        return new ModelPricingInfo(pricing.inputCostPerToken(), pricing.outputCostPerToken());
    }

    public static java.util.Set<String> getRegisteredModels() {
        return new java.util.HashSet<>(MODEL_PRICINGS.keySet());
    }

    public static void removeModelPricing(String model) {
        MODEL_PRICINGS.remove(model);
    }

    public static void clearCustomPricing() {
        logger.warning("clearCustomPricing() is not fully implemented");
    }

    public record ModelPricingInfo(double inputCostPerToken, double outputCostPerToken) {
        public double calculateTotalCost(int inputTokens, int outputTokens) {
            return (inputTokens / 1000.0) * inputCostPerToken +
                   (outputTokens / 1000.0) * outputCostPerToken;
        }

        @Override
        public String toString() {
            return String.format("$%.6f/1K input, $%.6f/1K output",
                inputCostPerToken, outputCostPerToken);
        }
    }
}
