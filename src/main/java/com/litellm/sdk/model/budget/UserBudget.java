package com.litellm.sdk.model.budget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserBudget(
        String userId,
        double totalBudget,
        BudgetDuration duration,
        long createdAt,
        long resetAt,
        double currentCost,
        Map<String, Double> modelCosts
) {
    public static UserBudget create(String userId, double totalBudget, BudgetDuration duration) {
        long now = Instant.now().toEpochMilli();
        long resetAt = duration.getNextResetTime(now);

        return new UserBudget(
            userId,
            totalBudget,
            duration,
            now,
            resetAt,
            0.0,
            new ConcurrentHashMap<>()
        );
    }

    public boolean shouldReset(long currentTimeMs) {
        return currentTimeMs >= resetAt && duration != BudgetDuration.NONE;
    }

    public boolean wouldExceedBudget(double costToAdd) {
        return currentCost + costToAdd > totalBudget;
    }

    public double getRemainingBudget() {
        return Math.max(0, totalBudget - currentCost);
    }

    public double getModelCost(String model) {
        return modelCosts.getOrDefault(model, 0.0);
    }

    public UserBudget updateCost(double additionalCost, String model, long currentTimeMs) {
        double newCurrentCost = currentCost + additionalCost;
        Map<String, Double> newModelCosts = new ConcurrentHashMap<>(modelCosts);
        newModelCosts.put(model, newModelCosts.getOrDefault(model, 0.0) + additionalCost);

        return new UserBudget(
            userId,
            totalBudget,
            duration,
            createdAt,
            resetAt,
            newCurrentCost,
            newModelCosts
        );
    }

    public UserBudget resetCost(long currentTimeMs) {
        if (!shouldReset(currentTimeMs)) {
            return this;
        }

        long newResetAt = duration.getNextResetTime(currentTimeMs);

        return new UserBudget(
            userId,
            totalBudget,
            duration,
            createdAt,
            newResetAt,
            0.0,
            new ConcurrentHashMap<>()
        );
    }

    public UserBudget updateTotalBudget(double newTotalBudget) {
        return new UserBudget(
            userId,
            newTotalBudget,
            duration,
            createdAt,
            resetAt,
            currentCost,
            modelCosts
        );
    }

    public UserBudget updateDuration(BudgetDuration newDuration, long currentTimeMs) {
        long newResetAt = newDuration.getNextResetTime(currentTimeMs);

        return new UserBudget(
            userId,
            totalBudget,
            newDuration,
            createdAt,
            newResetAt,
            currentCost,
            modelCosts
        );
    }

    @Override
    public String toString() {
        return "UserBudget{" +
                "userId='" + userId + '\'' +
                ", totalBudget=" + totalBudget +
                ", duration=" + duration +
                ", currentCost=" + currentCost +
                ", remainingBudget=" + getRemainingBudget() +
                ", resetAt=" + java.time.Instant.ofEpochMilli(resetAt) +
                '}';
    }
}
