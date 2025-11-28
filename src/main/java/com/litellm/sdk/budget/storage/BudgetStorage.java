package com.litellm.sdk.budget.storage;

import com.litellm.sdk.model.budget.BudgetInfo;
import com.litellm.sdk.model.budget.UserBudget;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BudgetStorage {
    void saveUserBudget(UserBudget budget) throws BudgetStorageException;

    Optional<UserBudget> getUserBudget(String userId) throws BudgetStorageException;

    void deleteUserBudget(String userId) throws BudgetStorageException;

    List<UserBudget> getAllUserBudgets() throws BudgetStorageException;

    BudgetInfo getBudgetInfo() throws BudgetStorageException;

    boolean userExists(String userId) throws BudgetStorageException;

    List<String> getAllUserIds() throws BudgetStorageException;

    CompletableFuture<Void> saveUserBudgetAsync(UserBudget budget);

    CompletableFuture<Optional<UserBudget>> getUserBudgetAsync(String userId);

    void saveAllUserBudgets(List<UserBudget> budgets) throws BudgetStorageException;

    void clearAll() throws BudgetStorageException;

    String getStorageType();
}
