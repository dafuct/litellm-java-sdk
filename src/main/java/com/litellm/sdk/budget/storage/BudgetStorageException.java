package com.litellm.sdk.budget.storage;

public class BudgetStorageException extends Exception {
    public BudgetStorageException(String message) {
        super(message);
    }

    public BudgetStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
