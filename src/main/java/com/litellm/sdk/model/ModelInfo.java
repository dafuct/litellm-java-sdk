package com.litellm.sdk.model;

public record ModelInfo(
    String model,
    String provider,
    String mode,
    double inputCostPerToken,
    double outputCostPerToken,
    int maxInputTokens,
    int maxOutputTokens,
    int maxTokens,
    boolean supportsFunctionCalling,
    boolean supportsVision,
    boolean supportsAudioInput,
    boolean supportsAudioOutput,
    boolean supportsSystemMessages,
    boolean supportsPromptCaching,
    double inputCostPerPixel,
    double outputCostPerPixel,
    double outputCostPerImage,
    double inputCostPerAudioToken,
    double outputCostPerReasoningToken
) {
    public boolean hasInputCost() {
        return inputCostPerToken > 0 || inputCostPerPixel > 0 || inputCostPerAudioToken > 0;
    }

    public boolean hasOutputCost() {
        return outputCostPerToken > 0 || outputCostPerImage > 0 || outputCostPerReasoningToken > 0;
    }

    public double getInputCostForTokens(int tokens) {
        return inputCostPerToken * tokens;
    }

    public double getOutputCostForTokens(int tokens) {
        return outputCostPerToken * tokens;
    }

    public boolean hasPricing() {
        return hasInputCost() || hasOutputCost();
    }
}
