package com.litellm.sdk.budget.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.litellm.sdk.model.budget.BudgetInfo;
import com.litellm.sdk.model.budget.UserBudget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMemoryBudgetStorage implements BudgetStorage {
    private static final Logger logger = Logger.getLogger(InMemoryBudgetStorage.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    private final Map<String, UserBudget> userBudgets;
    private final ReadWriteLock lock;
    private final String dataFilePath;
    private final boolean autoSave;

    public InMemoryBudgetStorage() {
        this(null, false);
    }

    public InMemoryBudgetStorage(String dataFilePath, boolean autoSave) {
        this.userBudgets = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.dataFilePath = dataFilePath;
        this.autoSave = autoSave;

        if (dataFilePath != null) {
            loadFromFile();
        }
    }

    @Override
    public void saveUserBudget(UserBudget budget) throws BudgetStorageException {
        lock.writeLock().lock();
        try {
            userBudgets.put(budget.userId(), budget);
            if (autoSave) {
                saveToFile();
            }
        } catch (Exception e) {
            String msg = "Failed to save user budget for user: " + budget.userId();
            logger.log(Level.SEVERE, msg, e);
            throw new BudgetStorageException(msg, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<UserBudget> getUserBudget(String userId) throws BudgetStorageException {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(userBudgets.get(userId));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteUserBudget(String userId) throws BudgetStorageException {
        lock.writeLock().lock();
        try {
            userBudgets.remove(userId);
            if (autoSave) {
                saveToFile();
            }
        } catch (Exception e) {
            String msg = "Failed to delete user budget for user: " + userId;
            logger.log(Level.SEVERE, msg, e);
            throw new BudgetStorageException(msg, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<UserBudget> getAllUserBudgets() throws BudgetStorageException {
        lock.readLock().lock();
        try {
            return new ArrayList<>(userBudgets.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public BudgetInfo getBudgetInfo() throws BudgetStorageException {
        lock.readLock().lock();
        try {
            return BudgetInfo.of(userBudgets);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean userExists(String userId) throws BudgetStorageException {
        lock.readLock().lock();
        try {
            return userBudgets.containsKey(userId);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<String> getAllUserIds() throws BudgetStorageException {
        lock.readLock().lock();
        try {
            return new ArrayList<>(userBudgets.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public CompletableFuture<Void> saveUserBudgetAsync(UserBudget budget) {
        return CompletableFuture.runAsync(() -> {
            try {
                saveUserBudget(budget);
            } catch (BudgetStorageException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<UserBudget>> getUserBudgetAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getUserBudget(userId);
            } catch (BudgetStorageException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void saveAllUserBudgets(List<UserBudget> budgets) throws BudgetStorageException {
        lock.writeLock().lock();
        try {
            userBudgets.clear();
            for (UserBudget budget : budgets) {
                userBudgets.put(budget.userId(), budget);
            }
            if (autoSave) {
                saveToFile();
            }
        } catch (Exception e) {
            String msg = "Failed to save all user budgets";
            logger.log(Level.SEVERE, msg, e);
            throw new BudgetStorageException(msg, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clearAll() throws BudgetStorageException {
        lock.writeLock().lock();
        try {
            userBudgets.clear();
            if (autoSave) {
                saveToFile();
            }
        } catch (Exception e) {
            String msg = "Failed to clear all budget data";
            logger.log(Level.SEVERE, msg, e);
            throw new BudgetStorageException(msg, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String getStorageType() {
        return "in-memory" + (dataFilePath != null ? "-with-persistence" : "");
    }

    private void loadFromFile() {
        if (dataFilePath == null) return;

        Path path = Paths.get(dataFilePath);
        if (!Files.exists(path)) {
            logger.info("Budget data file does not exist: " + dataFilePath);
            return;
        }

        lock.writeLock().lock();
        try {
            String json = Files.readString(path);
            UserBudget[] budgets = objectMapper.readValue(json, UserBudget[].class);

            userBudgets.clear();
            for (UserBudget budget : budgets) {
                userBudgets.put(budget.userId(), budget);
            }

            logger.info("Loaded " + budgets.length + " budgets from file: " + dataFilePath);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load budget data from file: " + dataFilePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveToFile() {
        if (dataFilePath == null) return;

        lock.readLock().lock();
        try {
            Path path = Paths.get(dataFilePath);
            Files.createDirectories(path.getParent());

            String tempFilePath = dataFilePath + ".tmp";
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(tempFilePath), userBudgets.values());

            Files.move(Paths.get(tempFilePath), path,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save budget data to file: " + dataFilePath, e);
        } finally {
            lock.readLock().unlock();
        }
    }
}
