package com.litellm.sdk;

import com.litellm.sdk.config.LiteLLMConfig;
import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.token.*;
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

    public static List<Integer> encode(String model, String text) {
        return Tokenizer.encode(model, text);
    }

    public static String decode(String model, List<Integer> tokens) {
        return Tokenizer.decode(model, tokens);
    }

    public static int tokenCounter(String model, String text) {
        return Tokenizer.tokenCounter(model, text);
    }

    public static int tokenCounter(String model, List<String> messages) {
        return Tokenizer.tokenCounter(model, messages);
    }

    public static double costPerToken(String model, int promptTokens, int completionTokens) {
        return CostCalculator.costPerToken(model, promptTokens, completionTokens);
    }

    public static double completionCost(String model, int promptTokens, int completionTokens) {
        return CostCalculator.completionCost(model, promptTokens, completionTokens);
    }

    public static double calculateCost(String model, int promptTokens, int completionTokens) {
        return CostCalculator.calculateCost(model, promptTokens, completionTokens);
    }

    public static double calculateCost(String model, String prompt, String completion) {
        return CostCalculator.calculateCost(model, prompt, completion);
    }

    public static int getMaxTokens(String model) {
        return CostCalculator.getMaxTokens(model);
    }

    public static boolean hasPricing(String model) {
        return CostCalculator.hasPricing(model);
    }

    public static Map<String, Double> getModelCost() {
        return CostCalculator.getModelCost();
    }

    public static Set<String> getRegisteredModels() {
        return CostCalculator.getRegisteredModels();
    }

    public static void registerModelPricing(String model, double inputCost, double outputCost) {
        CostCalculator.registerModelPricing(model, inputCost, outputCost);
    }
}
