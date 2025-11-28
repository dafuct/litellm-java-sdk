package com.litellm.sdk.budget.util;

import com.litellm.sdk.config.BudgetConfig;
import com.litellm.sdk.model.budget.BudgetInfo;
import com.litellm.sdk.model.budget.CostInfo;
import com.litellm.sdk.model.budget.UserBudget;
import com.litellm.sdk.budget.storage.BudgetStorage;
import com.litellm.sdk.error.BudgetExceededException;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

public class BudgetValidator {
    private static final Logger logger = Logger.getLogger(BudgetValidator.class.getName());

    private final BudgetStorage storage;

    public BudgetValidator(BudgetStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("BudgetStorage cannot be null");
        }
        this.storage = storage;
    }

    public ValidationResult validateBudget(String userId, CostInfo costInfo) throws BudgetExceededException {
        if (costInfo == null || !costInfo.hasCost()) {
            return ValidationResult.valid();
        }

        if (!validateGlobalBudget(costInfo.totalCost())) {
            throw new BudgetExceededException(
                "Global budget limit exceeded. Current: " + BudgetConfig.getCurrentCost() +
                ", Max: " + BudgetConfig.getMaxBudget() +
                ", Would be: " + (BudgetConfig.getCurrentCost() + costInfo.totalCost())
            );
        }

        if (userId != null && !userId.trim().isEmpty()) {
            UserBudget userBudget = getUserBudget(userId);
            if (userBudget != null) {
                UserBudget currentBudget = resetBudgetIfNeeded(userBudget);

                if (currentBudget.wouldExceedBudget(costInfo.totalCost())) {
                    throw new BudgetExceededException(
                        "User budget exceeded for user: " + userId +
                        ". Current: " + currentBudget.currentCost() +
                        ", Max: " + currentBudget.totalBudget() +
                        ", Would be: " + (currentBudget.currentCost() + costInfo.totalCost())
                    );
                }

                return ValidationResult.valid()
                    .withUserBudget(currentBudget);
            }
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateProjectedBudget(String userId, CostInfo costInfo) throws BudgetExceededException {
        if (costInfo == null || !costInfo.hasCost()) {
            return ValidationResult.valid();
        }

        if (BudgetConfig.wouldExceedGlobalBudget(costInfo.totalCost())) {
            throw new BudgetExceededException(
                "Projected cost would exceed global budget. Max: " + BudgetConfig.getMaxBudget() +
                ", Projected: " + (BudgetConfig.getCurrentCost() + costInfo.totalCost())
            );
        }

        if (userId != null && !userId.trim().isEmpty()) {
            Optional<UserBudget> optBudget;
            try {
                optBudget = storage.getUserBudget(userId);
            } catch (Exception e) {
                logger.warning("Failed to get user budget for validation: " + userId);
                return ValidationResult.valid();
            }

            if (optBudget.isPresent()) {
                UserBudget budget = resetBudgetIfNeeded(optBudget.get());

                if (budget.wouldExceedBudget(costInfo.totalCost())) {
                    throw new BudgetExceededException(
                        "Projected cost would exceed user budget for: " + userId +
                        ". Max: " + budget.totalBudget() +
                        ", Current: " + budget.currentCost() +
                        ", Projected: " + (budget.currentCost() + costInfo.totalCost())
                    );
                }
            }
        }

        return ValidationResult.valid();
    }

    public boolean validateGlobalBudget(double cost) {
        return !BudgetConfig.wouldExceedGlobalBudget(cost);
    }

    public boolean validateUserBudget(String userId, double cost) {
        if (userId == null || userId.trim().isEmpty()) {
            return true;
        }

        try {
            Optional<UserBudget> optBudget = storage.getUserBudget(userId);
            if (optBudget.isEmpty()) {
                return true;
            }

            UserBudget budget = resetBudgetIfNeeded(optBudget.get());
            return !budget.wouldExceedBudget(cost);
        } catch (Exception e) {
            logger.warning("Failed to validate user budget for: " + userId + ", error: " + e.getMessage());
            return true;
        }
    }

    public UserBudget getUserBudget(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        try {
            Optional<UserBudget> optBudget = storage.getUserBudget(userId);
            return optBudget.map(this::resetBudgetIfNeeded).orElse(null);
        } catch (Exception e) {
            logger.warning("Failed to get user budget for: " + userId + ", error: " + e.getMessage());
            return null;
        }
    }

    public BudgetInfo getBudgetInfo() {
        try {
            return storage.getBudgetInfo();
        } catch (Exception e) {
            logger.warning("Failed to get budget info: " + e.getMessage());
            return BudgetInfo.empty();
        }
    }

    public boolean isBudgetTrackingEnabled() {
        return BudgetConfig.isBudgetTrackingEnabled();
    }

    private UserBudget resetBudgetIfNeeded(UserBudget budget) {
        long now = Instant.now().toEpochMilli();
        if (budget.shouldReset(now)) {
            logger.info("Resetting budget for user: " + budget.userId());
            UserBudget resetBudget = budget.resetCost(now);

            try {
                storage.saveUserBudget(resetBudget);
            } catch (Exception e) {
                logger.warning("Failed to save reset budget for user: " + budget.userId());
            }

            return resetBudget;
        }
        return budget;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final UserBudget userBudget;
        private final BudgetInfo budgetInfo;

        private ValidationResult(boolean valid, String message, UserBudget userBudget, BudgetInfo budgetInfo) {
            this.valid = valid;
            this.message = message;
            this.userBudget = userBudget;
            this.budgetInfo = budgetInfo;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null, null, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message, null, null);
        }

        public static ValidationResult validWithMessage(String message) {
            return new ValidationResult(true, message, null, null);
        }

        public ValidationResult withUserBudget(UserBudget budget) {
            return new ValidationResult(valid, message, budget, budgetInfo);
        }

        public ValidationResult withBudgetInfo(BudgetInfo info) {
            return new ValidationResult(valid, message, userBudget, info);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public UserBudget getUserBudget() {
            return userBudget;
        }

        public BudgetInfo getBudgetInfo() {
            return budgetInfo;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    ", userBudget=" + userBudget +
                    '}';
        }
    }
}
