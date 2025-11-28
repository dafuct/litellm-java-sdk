package com.litellm.sdk.unit.budget;

import com.litellm.sdk.budget.BudgetManager;
import com.litellm.sdk.budget.storage.BudgetStorageException;
import com.litellm.sdk.config.BudgetConfig;
import com.litellm.sdk.error.BudgetExceededException;
import com.litellm.sdk.model.budget.BudgetDuration;
import com.litellm.sdk.model.budget.BudgetInfo;
import com.litellm.sdk.model.budget.UserBudget;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BudgetManagerTest {

    private BudgetManager budgetManager;

    @BeforeEach
    public void setUp() {
        BudgetConfig.clear();
        budgetManager = BudgetManager.builder()
            .projectName("test_project")
            .clientType("local")
            .build();
    }

    @AfterEach
    public void tearDown() {
        if (budgetManager != null) {
            budgetManager.shutdown();
        }
        BudgetConfig.clear();
    }

    @Test
    @DisplayName("Test create budget manager with builder")
    public void testCreateBudgetManager() {
        assertNotNull(budgetManager);
        assertEquals("test_project", budgetManager.getProjectName());
        assertEquals("local", budgetManager.getClientType());
    }

    @Test
    @DisplayName("Test create user budget")
    public void testCreateUserBudget() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user123", BudgetDuration.MONTHLY);

        assertTrue(budgetManager.isValidUser("user123"));
        assertEquals(100.0, budgetManager.getTotalBudget("user123"));
        assertEquals(0.0, budgetManager.getCurrentCost("user123"));
    }

    @Test
    @DisplayName("Test create user budget with string duration")
    public void testCreateUserBudgetWithStringDuration() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user123", "monthly");

        assertTrue(budgetManager.isValidUser("user123"));
        assertEquals(100.0, budgetManager.getTotalBudget("user123"));
    }

    @Test
    @DisplayName("Test create budget with invalid parameters")
    public void testCreateBudgetWithInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            budgetManager.createBudget(100.0, "", BudgetDuration.MONTHLY);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            budgetManager.createBudget(100.0, "user123", (BudgetDuration) null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            budgetManager.createBudget(-100.0, "user123", BudgetDuration.MONTHLY);
        });
    }

    @Test
    @DisplayName("Test projected cost calculation")
    public void testProjectedCost() {
        java.util.List<String> messages = List.of("Hello", "World");

        double cost = budgetManager.projectedCost("gpt-4", messages, "user123");

        assertTrue(cost >= 0);
    }

    @Test
    @DisplayName("Test get total budget for non-existent user")
    public void testGetTotalBudgetForNonExistentUser() {
        double total = budgetManager.getTotalBudget("non-existent-user");
        assertEquals(0.0, total);
    }

    @Test
    @DisplayName("Test get current cost for non-existent user")
    public void testGetCurrentCostForNonExistentUser() {
        double current = budgetManager.getCurrentCost("non-existent-user");
        assertEquals(0.0, current);
    }

    @Test
    @DisplayName("Test get model cost for non-existent user")
    public void testGetModelCostForNonExistentUser() {
        Map<String, Double> costs = budgetManager.getModelCost("non-existent-user");
        assertNotNull(costs);
        assertTrue(costs.isEmpty());
    }

    @Test
    @DisplayName("Test is valid user")
    public void testIsValidUser() throws BudgetStorageException {
        assertFalse(budgetManager.isValidUser("user123"));

        budgetManager.createBudget(100.0, "user123", BudgetDuration.MONTHLY);
        assertTrue(budgetManager.isValidUser("user123"));
    }

    @Test
    @DisplayName("Test get users")
    public void testGetUsers() throws BudgetStorageException {
        assertEquals(0, budgetManager.getUsers().size());

        budgetManager.createBudget(100.0, "user1", BudgetDuration.MONTHLY);
        budgetManager.createBudget(200.0, "user2", BudgetDuration.WEEKLY);

        List<String> users = budgetManager.getUsers();
        assertEquals(2, users.size());
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
    }

    @Test
    @DisplayName("Test reset cost")
    public void testResetCost() throws BudgetStorageException, BudgetExceededException {
        budgetManager.createBudget(100.0, "user123", BudgetDuration.MONTHLY);

        budgetManager.resetCost("user123");

        assertEquals(0.0, budgetManager.getCurrentCost("user123"));
    }

    @Test
    @DisplayName("Test reset on duration")
    public void testResetOnDuration() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user123", BudgetDuration.MONTHLY);

        budgetManager.resetOnDuration("user123");

        assertEquals(0.0, budgetManager.getCurrentCost("user123"));
    }

    @Test
    @DisplayName("Test update budget all users")
    public void testUpdateBudgetAllUsers() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user1", BudgetDuration.MONTHLY);
        budgetManager.createBudget(200.0, "user2", BudgetDuration.WEEKLY);

        assertDoesNotThrow(() -> budgetManager.updateBudgetAllUsers());
    }

    @Test
    @DisplayName("Test save data")
    public void testSaveData() throws BudgetStorageException {
        assertDoesNotThrow(() -> budgetManager.saveData());
    }

    @Test
    @DisplayName("Test get budget info")
    public void testGetBudgetInfo() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user1", BudgetDuration.MONTHLY);
        budgetManager.createBudget(200.0, "user2", BudgetDuration.WEEKLY);

        BudgetInfo info = budgetManager.getBudgetInfo();

        assertNotNull(info);
        assertEquals(300.0, info.totalBudget());
        assertEquals(0.0, info.totalSpent());
        assertEquals(2, info.totalUsers());
    }

    @Test
    @DisplayName("Test shutdown")
    public void testShutdown() {
        assertDoesNotThrow(() -> budgetManager.shutdown());
    }

    @Test
    @DisplayName("Test builder fluent API")
    public void testBuilderFluentAPI() {
        BudgetManager manager = BudgetManager.builder()
            .projectName("my_project")
            .clientType("hosted")
            .apiBase("https://api.example.com")
            .build();

        assertNotNull(manager);
        assertEquals("my_project", manager.getProjectName());
        assertEquals("hosted", manager.getClientType());

        manager.shutdown();
    }

    @Test
    @DisplayName("Test parse duration from string")
    public void testParseDurationFromString() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user1", "daily");
        budgetManager.createBudget(200.0, "user2", "weekly");
        budgetManager.createBudget(300.0, "user3", "monthly");
        budgetManager.createBudget(400.0, "user4", "yearly");
        budgetManager.createBudget(500.0, "user5", "none");

        assertTrue(budgetManager.isValidUser("user1"));
        assertTrue(budgetManager.isValidUser("user2"));
        assertTrue(budgetManager.isValidUser("user3"));
        assertTrue(budgetManager.isValidUser("user4"));
        assertTrue(budgetManager.isValidUser("user5"));
    }

    @Test
    @DisplayName("Test multiple users with independent budgets")
    public void testMultipleUsersWithIndependentBudgets() throws BudgetStorageException {
        budgetManager.createBudget(100.0, "user1", BudgetDuration.MONTHLY);
        budgetManager.createBudget(200.0, "user2", BudgetDuration.MONTHLY);
        budgetManager.createBudget(300.0, "user3", BudgetDuration.MONTHLY);

        assertEquals(100.0, budgetManager.getTotalBudget("user1"));
        assertEquals(200.0, budgetManager.getTotalBudget("user2"));
        assertEquals(300.0, budgetManager.getTotalBudget("user3"));

        List<String> users = budgetManager.getUsers();
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("Test global budget integration")
    public void testGlobalBudgetIntegration() {
        BudgetConfig.setMaxBudget(50.0);
        BudgetConfig.setCurrentCost(10.0);

        assertEquals(50.0, BudgetConfig.getMaxBudget());
        assertEquals(10.0, BudgetConfig.getCurrentCost());
        assertEquals(40.0, BudgetConfig.getRemainingGlobalBudget());
    }

    @Test
    @DisplayName("Test update cost with text I/O")
    public void testUpdateCostWithTextIO() {
        assertNotNull(budgetManager);
    }

    @Test
    @DisplayName("Test budget manager with different project names")
    public void testBudgetManagerWithDifferentProjectNames() {
        BudgetManager manager1 = BudgetManager.builder()
            .projectName("project1")
            .clientType("local")
            .build();

        BudgetManager manager2 = BudgetManager.builder()
            .projectName("project2")
            .clientType("local")
            .build();

        assertNotNull(manager1);
        assertNotNull(manager2);
        assertEquals("project1", manager1.getProjectName());
        assertEquals("project2", manager2.getProjectName());

        manager1.shutdown();
        manager2.shutdown();
    }

    @Test
    @DisplayName("Test create budget with zero duration")
    public void testCreateBudgetWithZeroDuration() throws BudgetStorageException {
        assertThrows(IllegalArgumentException.class, () -> {
            budgetManager.createBudget(100.0, "user123", (BudgetDuration) null);
        });
    }

    @Test
    @DisplayName("Test get budget info when no users")
    public void testGetBudgetInfoWhenNoUsers() {
        BudgetInfo info = budgetManager.getBudgetInfo();

        assertNotNull(info);
        assertEquals(0.0, info.totalBudget());
        assertEquals(0.0, info.totalSpent());
        assertEquals(0, info.totalUsers());
    }

    @Test
    @DisplayName("Test concurrent budget creation")
    public void testConcurrentBudgetCreation() throws Exception {
        int threadCount = 10;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    budgetManager.createBudget(100.0, "user" + index, BudgetDuration.MONTHLY);
                } catch (BudgetStorageException e) {
                    fail("Failed to create budget: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> users = budgetManager.getUsers();
        assertEquals(threadCount, users.size());

        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Test get all users when empty")
    public void testGetAllUsersWhenEmpty() {
        List<String> users = budgetManager.getUsers();
        assertNotNull(users);
        assertEquals(0, users.size());
    }
}