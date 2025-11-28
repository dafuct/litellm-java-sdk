package com.litellm.sdk.model.budget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BudgetInfo(
        double totalBudget,
        double totalSpent,
        double remainingBudget,
        Map<String, UserBudget> userBudgets,
        Map<String, Double> modelCosts,
        int totalUsers
) {
    public static BudgetInfo of(Map<String, UserBudget> userBudgets) {
        double totalBudget = 0.0;
        double totalSpent = 0.0;
        Map<String, Double> modelCosts = new java.util.concurrent.ConcurrentHashMap<>();

        for (UserBudget budget : userBudgets.values()) {
            totalBudget += budget.totalBudget();
            totalSpent += budget.currentCost();

            for (Map.Entry<String, Double> entry : budget.modelCosts().entrySet()) {
                modelCosts.put(entry.getKey(),
                    modelCosts.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
            }
        }

        double remainingBudget = Math.max(0, totalBudget - totalSpent);

        return new BudgetInfo(
            totalBudget,
            totalSpent,
            remainingBudget,
            userBudgets,
            modelCosts,
            userBudgets.size()
        );
    }

    public static BudgetInfo empty() {
        return new BudgetInfo(
            0.0,
            0.0,
            0.0,
            Map.of(),
            Map.of(),
            0
        );
    }

    public double getBudgetUsedPercentage() {
        if (totalBudget <= 0) return 0.0;
        return (totalSpent / totalBudget) * 100.0;
    }

    public boolean isBudgetExceeded() {
        return totalSpent > totalBudget;
    }

    @Override
    public String toString() {
        return "BudgetInfo{" +
                "totalBudget=" + totalBudget +
                ", totalSpent=" + totalSpent +
                ", remainingBudget=" + remainingBudget +
                ", totalUsers=" + totalUsers +
                ", budgetUsedPercentage=" + String.format("%.2f%%", getBudgetUsedPercentage()) +
                '}';
    }
}
