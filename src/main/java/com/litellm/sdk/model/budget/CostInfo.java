package com.litellm.sdk.model.budget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CostInfo(
        double totalCost,
        String model,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        Double costPerToken
) {
    public static CostInfo fromTokens(String model, Integer promptTokens, Integer completionTokens, Double costPerToken) {
        int totalTokens = ((promptTokens != null ? promptTokens : 0) + (completionTokens != null ? completionTokens : 0));
        double totalCost = totalTokens * (costPerToken != null ? costPerToken : 0.0);

        return new CostInfo(
            totalCost,
            model,
            promptTokens,
            completionTokens,
            totalTokens,
            costPerToken
        );
    }

    public static CostInfo fromTotalCost(String model, double totalCost) {
        return new CostInfo(
            totalCost,
            model,
            null,
            null,
            null,
            null
        );
    }

    public static CostInfo empty(String model) {
        return new CostInfo(0.0, model, null, null, null, null);
    }

    public boolean hasCost() {
        return totalCost > 0.0;
    }

    @Override
    public String toString() {
        return "CostInfo{" +
                "model='" + model + '\'' +
                ", totalCost=" + totalCost +
                ", totalTokens=" + totalTokens +
                '}';
    }
}
