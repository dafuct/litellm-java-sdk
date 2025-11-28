package com.litellm.sdk.config;

import java.util.concurrent.atomic.AtomicReference;

public final class BudgetConfig {
    private static final AtomicReference<Double> maxBudget = new AtomicReference<>(null);
    private static final AtomicReference<Double> currentCost = new AtomicReference<>(0.0);
    private static final AtomicReference<Boolean> budgetTrackingEnabled = new AtomicReference<>(true);
    private static final AtomicReference<String> projectName = new AtomicReference<>("default");

    private BudgetConfig() {
    }

    public static void setMaxBudget(double budget) {
        maxBudget.set(budget);
    }

    public static Double getMaxBudget() {
        return maxBudget.get();
    }

    public static double addToCurrentCost(double costToAdd) {
        double current;
        double updated;
        do {
            current = currentCost.get();
            updated = current + costToAdd;
        } while (!currentCost.compareAndSet(current, updated));
        return updated;
    }

    public static void setCurrentCost(double newCost) {
        currentCost.set(newCost);
    }

    public static double getCurrentCost() {
        return currentCost.get();
    }

    public static void setBudgetTrackingEnabled(boolean enabled) {
        budgetTrackingEnabled.set(enabled);
    }

    public static boolean isBudgetTrackingEnabled() {
        return budgetTrackingEnabled.get();
    }

    public static void setProjectName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            projectName.set(name);
        }
    }

    public static String getProjectName() {
        return projectName.get();
    }

    public static boolean wouldExceedGlobalBudget(double costToCheck) {
        Double max = maxBudget.get();
        if (max == null) {
            return false;
        }
        return (currentCost.get() + costToCheck) > max;
    }

    public static Double getRemainingGlobalBudget() {
        Double max = maxBudget.get();
        if (max == null) {
            return null;
        }
        return Math.max(0, max - currentCost.get());
    }

    public static void resetCurrentCost() {
        currentCost.set(0.0);
    }

    public static void clear() {
        maxBudget.set(null);
        currentCost.set(0.0);
        projectName.set("default");
        budgetTrackingEnabled.set(true);
    }

    @Override
    public String toString() {
        return "BudgetConfig{" +
                "maxBudget=" + maxBudget.get() +
                ", currentCost=" + currentCost.get() +
                ", projectName='" + projectName.get() + '\'' +
                ", budgetTrackingEnabled=" + budgetTrackingEnabled.get() +
                ", remainingBudget=" + getRemainingGlobalBudget() +
                '}';
    }
}
