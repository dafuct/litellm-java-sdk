package com.litellm.sdk.budget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.litellm.sdk.budget.storage.BudgetStorage;
import com.litellm.sdk.budget.storage.BudgetStorageException;
import com.litellm.sdk.budget.storage.InMemoryBudgetStorage;
import com.litellm.sdk.budget.util.BudgetValidator;
import com.litellm.sdk.budget.util.CostCalculator;
import com.litellm.sdk.config.BudgetConfig;
import com.litellm.sdk.error.BudgetExceededException;
import com.litellm.sdk.model.budget.*;
import com.litellm.sdk.model.common.Usage;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.TextCompletionResponse;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BudgetManager {
    private static final Logger logger = Logger.getLogger(BudgetManager.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String projectName;
    private final String clientType;
    private final String apiBase;
    private final BudgetStorage storage;
    private final BudgetValidator validator;
    private final ScheduledExecutorService scheduler;

    private BudgetManager(Builder builder) {
        this.projectName = builder.projectName != null ? builder.projectName : "default";
        this.clientType = builder.clientType != null ? builder.clientType : "local";
        this.apiBase = builder.apiBase;

        this.storage = createStorage();

        this.validator = new BudgetValidator(storage);

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "budget-manager-scheduler");
            t.setDaemon(true);
            return t;
        });

        schedulePeriodicResets();
    }

    public static Builder builder() {
        return new Builder();
    }

    private BudgetStorage createStorage() {
        if ("local".equals(clientType)) {
            String dataFile = System.getProperty("user.home") + "/.litellm/budget_" + projectName + ".json";
            return new InMemoryBudgetStorage(dataFile, true);
        } else if ("hosted".equals(clientType)) {
            logger.warning("Hosted mode not fully implemented, using in-memory storage");
            return new InMemoryBudgetStorage();
        } else {
            throw new IllegalArgumentException("Unknown client type: " + clientType);
        }
    }

    public void createBudget(double totalBudget, String user, BudgetDuration duration) throws BudgetStorageException {
        if (user == null || user.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (duration == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
        if (totalBudget <= 0) {
            throw new IllegalArgumentException("Total budget must be positive");
        }

        UserBudget budget = UserBudget.create(user, totalBudget, duration);
        storage.saveUserBudget(budget);

        logger.info("Created budget for user: " + user + ", amount: " + totalBudget + ", duration: " + duration);
    }

    public void createBudget(double totalBudget, String user, String durationStr) throws BudgetStorageException {
        BudgetDuration duration = parseDuration(durationStr);
        createBudget(totalBudget, user, duration);
    }

    public double projectedCost(String model, List<?> messages, String user) {
        CostInfo costInfo = CostCalculator.estimateCostFromMessages(model, messages);
        return costInfo.totalCost();
    }

    public double getTotalBudget(String user) {
        UserBudget budget = validator.getUserBudget(user);
        return budget != null ? budget.totalBudget() : 0.0;
    }

    public void updateCost(ChatCompletionResponse response, String user)
        throws BudgetExceededException, BudgetStorageException {
        if (response == null || user == null) {
            return;
        }

        Usage usage = response.usage();
        if (usage == null) {
            logger.warning("No usage information in response, cannot update cost");
            return;
        }

        String model = response.model();
        CostInfo costInfo = CostCalculator.calculateCost(model, usage);

        updateCostInternal(user, costInfo);
    }

    public void updateCost(TextCompletionResponse response, String user)
        throws BudgetExceededException, BudgetStorageException {
        if (response == null || user == null) {
            return;
        }

        Usage usage = response.usage();
        if (usage == null) {
            logger.warning("No usage information in response, cannot update cost");
            return;
        }

        String model = response.model();
        CostInfo costInfo = CostCalculator.calculateCost(model, usage);

        updateCostInternal(user, costInfo);
    }

    public void updateCost(String user, String model, String inputText, String outputText)
        throws BudgetExceededException, BudgetStorageException {
        if (user == null || model == null) {
            return;
        }

        CostInfo costInfo = CostCalculator.estimateCostFromText(model, inputText, outputText);
        updateCostInternal(user, costInfo);
    }

    private void updateCostInternal(String user, CostInfo costInfo)
        throws BudgetExceededException, BudgetStorageException {
        if (costInfo == null || !costInfo.hasCost()) {
            return;
        }

        validator.validateBudget(user, costInfo);

        BudgetConfig.addToCurrentCost(costInfo.totalCost());

        Optional<UserBudget> optBudget = storage.getUserBudget(user);
        if (optBudget.isPresent()) {
            UserBudget currentBudget = validator.getUserBudget(user);
            if (currentBudget != null) {
                UserBudget updatedBudget = currentBudget.updateCost(
                    costInfo.totalCost(),
                    costInfo.model(),
                    Instant.now().toEpochMilli()
                );
                storage.saveUserBudget(updatedBudget);
            }
        }

        logger.info("Updated cost for user: " + user + ", model: " + costInfo.model() +
                    ", cost: " + costInfo.totalCost());
    }

    public double getCurrentCost(String user) {
        UserBudget budget = validator.getUserBudget(user);
        return budget != null ? budget.currentCost() : 0.0;
    }

    public Map<String, Double> getModelCost(String user) {
        UserBudget budget = validator.getUserBudget(user);
        return budget != null ? Map.copyOf(budget.modelCosts()) : Map.of();
    }

    public boolean isValidUser(String user) {
        try {
            return storage.userExists(user);
        } catch (BudgetStorageException e) {
            logger.warning("Failed to check if user exists: " + user);
            return false;
        }
    }

    public List<String> getUsers() {
        try {
            return storage.getAllUserIds();
        } catch (BudgetStorageException e) {
            logger.warning("Failed to get all users: " + e.getMessage());
            return List.of();
        }
    }

    public void resetCost(String user) throws BudgetStorageException {
        if (user == null || user.trim().isEmpty()) {
            return;
        }

        UserBudget budget = validator.getUserBudget(user);
        if (budget != null) {
            UserBudget resetBudget = budget.resetCost(Instant.now().toEpochMilli());
            storage.saveUserBudget(resetBudget);
            logger.info("Reset cost for user: " + user);
        }
    }

    public void resetOnDuration(String user) throws BudgetStorageException {
        resetCost(user);
    }

    public void updateBudgetAllUsers() throws BudgetStorageException {
        List<UserBudget> budgets = storage.getAllUserBudgets();
        long now = Instant.now().toEpochMilli();

        for (UserBudget budget : budgets) {
            if (budget.shouldReset(now)) {
                UserBudget resetBudget = budget.resetCost(now);
                storage.saveUserBudget(resetBudget);
                logger.info("Auto-reset budget for user: " + budget.userId());
            }
        }
    }

    public void saveData() throws BudgetStorageException {
        if (storage instanceof InMemoryBudgetStorage) {
            logger.info("Data saved (in-memory storage)");
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public String getClientType() {
        return clientType;
    }

    public BudgetInfo getBudgetInfo() {
        return validator.getBudgetInfo();
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void schedulePeriodicResets() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateBudgetAllUsers();
            } catch (BudgetStorageException e) {
                logger.warning("Failed to update budgets during periodic check: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    private BudgetDuration parseDuration(String durationStr) {
        if (durationStr == null) {
            return BudgetDuration.NONE;
        }

        return switch (durationStr.toLowerCase(Locale.ROOT)) {
            case "daily" -> BudgetDuration.DAILY;
            case "weekly" -> BudgetDuration.WEEKLY;
            case "monthly" -> BudgetDuration.MONTHLY;
            case "yearly" -> BudgetDuration.YEARLY;
            default -> BudgetDuration.NONE;
        };
    }

    public static class Builder {
        private String projectName;
        private String clientType;
        private String apiBase;

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder clientType(String clientType) {
            this.clientType = clientType;
            return this;
        }

        public Builder apiBase(String apiBase) {
            this.apiBase = apiBase;
            return this;
        }

        public BudgetManager build() {
            return new BudgetManager(this);
        }
    }
}
