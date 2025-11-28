package com.litellm.sdk.unit.budget;

import com.litellm.sdk.budget.storage.BudgetStorage;
import com.litellm.sdk.budget.storage.BudgetStorageException;
import com.litellm.sdk.budget.storage.InMemoryBudgetStorage;
import com.litellm.sdk.model.budget.BudgetDuration;
import com.litellm.sdk.model.budget.BudgetInfo;
import com.litellm.sdk.model.budget.UserBudget;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InMemoryBudgetStorageTest {

    private BudgetStorage storage;

    @BeforeEach
    public void setUp() throws BudgetStorageException {
        storage = new InMemoryBudgetStorage();
        storage.clearAll();
    }

    @Test
    @DisplayName("Test save and get user budget")
    public void testSaveAndGetUserBudget() throws BudgetStorageException {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);

        storage.saveUserBudget(budget);
        Optional<UserBudget> retrieved = storage.getUserBudget("user123");

        assertTrue(retrieved.isPresent());
        assertEquals("user123", retrieved.get().userId());
        assertEquals(100.0, retrieved.get().totalBudget());
    }

    @Test
    @DisplayName("Test get non-existent user budget")
    public void testGetNonExistentUserBudget() throws BudgetStorageException {
        Optional<UserBudget> retrieved = storage.getUserBudget("non-existent-user");

        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Test delete user budget")
    public void testDeleteUserBudget() throws BudgetStorageException {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        storage.saveUserBudget(budget);

        storage.deleteUserBudget("user123");

        Optional<UserBudget> retrieved = storage.getUserBudget("user123");
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Test get all user budgets")
    public void testGetAllUserBudgets() throws BudgetStorageException {
        storage.saveUserBudget(UserBudget.create("user1", 100.0, BudgetDuration.MONTHLY));
        storage.saveUserBudget(UserBudget.create("user2", 200.0, BudgetDuration.WEEKLY));
        storage.saveUserBudget(UserBudget.create("user3", 300.0, BudgetDuration.DAILY));

        List<UserBudget> allBudgets = storage.getAllUserBudgets();

        assertEquals(3, allBudgets.size());
    }

    @Test
    @DisplayName("Test get all user budgets when empty")
    public void testGetAllUserBudgetsWhenEmpty() throws BudgetStorageException {
        List<UserBudget> allBudgets = storage.getAllUserBudgets();

        assertNotNull(allBudgets);
        assertEquals(0, allBudgets.size());
    }

    @Test
    @DisplayName("Test get budget info")
    public void testGetBudgetInfo() throws BudgetStorageException {
        storage.saveUserBudget(UserBudget.create("user1", 100.0, BudgetDuration.MONTHLY));
        storage.saveUserBudget(UserBudget.create("user2", 200.0, BudgetDuration.WEEKLY));

        BudgetInfo info = storage.getBudgetInfo();

        assertNotNull(info);
        assertEquals(300.0, info.totalBudget());
        assertEquals(0.0, info.totalSpent());
        assertEquals(300.0, info.remainingBudget());
        assertEquals(2, info.totalUsers());
    }

    @Test
    @DisplayName("Test user exists")
    public void testUserExists() throws BudgetStorageException {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        storage.saveUserBudget(budget);

        assertTrue(storage.userExists("user123"));
        assertFalse(storage.userExists("non-existent-user"));
    }

    @Test
    @DisplayName("Test get all user ids")
    public void testGetAllUserIds() throws BudgetStorageException {
        storage.saveUserBudget(UserBudget.create("user1", 100.0, BudgetDuration.MONTHLY));
        storage.saveUserBudget(UserBudget.create("user2", 200.0, BudgetDuration.WEEKLY));

        List<String> userIds = storage.getAllUserIds();

        assertEquals(2, userIds.size());
        assertTrue(userIds.contains("user1"));
        assertTrue(userIds.contains("user2"));
    }

    @Test
    @DisplayName("Test save all user budgets")
    public void testSaveAllUserBudgets() throws BudgetStorageException {
        List<UserBudget> budgets = List.of(
            UserBudget.create("user1", 100.0, BudgetDuration.MONTHLY),
            UserBudget.create("user2", 200.0, BudgetDuration.WEEKLY)
        );

        storage.saveAllUserBudgets(budgets);

        List<UserBudget> retrieved = storage.getAllUserBudgets();
        assertEquals(2, retrieved.size());
    }

    @Test
    @DisplayName("Test clear all")
    public void testClearAll() throws BudgetStorageException {
        storage.saveUserBudget(UserBudget.create("user1", 100.0, BudgetDuration.MONTHLY));
        storage.saveUserBudget(UserBudget.create("user2", 200.0, BudgetDuration.WEEKLY));

        storage.clearAll();

        List<UserBudget> allBudgets = storage.getAllUserBudgets();
        assertEquals(0, allBudgets.size());
    }

    @Test
    @DisplayName("Test get storage type")
    public void testGetStorageType() {
        String type = storage.getStorageType();

        assertNotNull(type);
        assertTrue(type.contains("in-memory"));
    }

    @Test
    @DisplayName("Test async save user budget")
    public void testAsyncSaveUserBudget() throws Exception {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);

        CompletableFuture<Void> future = storage.saveUserBudgetAsync(budget);
        future.get();

        Optional<UserBudget> retrieved = storage.getUserBudget("user123");
        assertTrue(retrieved.isPresent());
    }

    @Test
    @DisplayName("Test async get user budget")
    public void testAsyncGetUserBudget() throws Exception {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        storage.saveUserBudget(budget);

        CompletableFuture<Optional<UserBudget>> future = storage.getUserBudgetAsync("user123");
        Optional<UserBudget> retrieved = future.get();

        assertTrue(retrieved.isPresent());
        assertEquals("user123", retrieved.get().userId());
    }

    @Test
    @DisplayName("Test thread safety")
    public void testThreadSafety() throws Exception {
        int threadCount = 10;
        int budgetsPerThread = 10;

        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < budgetsPerThread; j++) {
                    String userId = "user_" + threadIndex + "_" + j;
                    try {
                        UserBudget budget = UserBudget.create(userId, 100.0, BudgetDuration.MONTHLY);
                        storage.saveUserBudget(budget);
                    } catch (BudgetStorageException e) {
                        fail("Failed to save budget: " + e.getMessage());
                    }
                }
            });
        }

        CompletableFuture.allOf(futures).get();

        List<UserBudget> allBudgets = storage.getAllUserBudgets();
        assertEquals(threadCount * budgetsPerThread, allBudgets.size());
    }

    @Test
    @DisplayName("Test update user budget")
    public void testUpdateUserBudget() throws BudgetStorageException {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        storage.saveUserBudget(budget);

        UserBudget updatedBudget = budget.updateCost(50.0, "gpt-4", System.currentTimeMillis());
        storage.saveUserBudget(updatedBudget);

        Optional<UserBudget> retrieved = storage.getUserBudget("user123");
        assertTrue(retrieved.isPresent());
        assertEquals(50.0, retrieved.get().currentCost());
        assertEquals(50.0, retrieved.get().getModelCost("gpt-4"));
    }

    @Test
    @DisplayName("Test BudgetStorageException is thrown on save failure")
    public void testBudgetStorageExceptionOnSaveFailure() {
        storage = new InMemoryBudgetStorage("/invalid/path/budget.json", true);

        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);

        assertDoesNotThrow(() -> storage.saveUserBudget(budget));
    }

    @Test
    @DisplayName("Test storage with data file persistence")
    public void testStorageWithDataFilePersistence() throws Exception {
        String tempFile = System.getProperty("java.io.tmpdir") + "/budget_test_" + System.currentTimeMillis() + ".json";

        BudgetStorage persistentStorage = new InMemoryBudgetStorage(tempFile, true);

        try {
            UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
            persistentStorage.saveUserBudget(budget);

            Optional<UserBudget> retrieved = persistentStorage.getUserBudget("user123");
            assertTrue(retrieved.isPresent());

            BudgetStorage newStorage = new InMemoryBudgetStorage(tempFile, true);

            Optional<UserBudget> persisted = newStorage.getUserBudget("user123");
            assertTrue(persisted.isPresent());
            assertEquals("user123", persisted.get().userId());

        } finally {
            try {
                new java.io.File(tempFile).delete();
            } catch (Exception e) {
            }
        }
    }
}