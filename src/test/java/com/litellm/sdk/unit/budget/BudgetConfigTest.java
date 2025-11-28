package com.litellm.sdk.unit.budget;

import com.litellm.sdk.config.BudgetConfig;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BudgetConfigTest {

    @BeforeEach
    public void setUp() {
        BudgetConfig.clear();
    }

    @AfterAll
    public void tearDown() {
        BudgetConfig.clear();
    }

    @Test
    @DisplayName("Test setting and getting max budget")
    public void testMaxBudget() {
        BudgetConfig.setMaxBudget(10.5);
        assertEquals(10.5, BudgetConfig.getMaxBudget());
    }

    @Test
    @DisplayName("Test adding to current cost")
    public void testAddToCurrentCost() {
        BudgetConfig.setCurrentCost(5.0);
        double newCost = BudgetConfig.addToCurrentCost(3.0);
        assertEquals(8.0, newCost);
        assertEquals(8.0, BudgetConfig.getCurrentCost());
    }

    @Test
    @DisplayName("Test setting current cost")
    public void testSetCurrentCost() {
        BudgetConfig.setCurrentCost(15.5);
        assertEquals(15.5, BudgetConfig.getCurrentCost());
    }

    @Test
    @DisplayName("Test budget tracking enabled flag")
    public void testBudgetTrackingEnabled() {
        assertTrue(BudgetConfig.isBudgetTrackingEnabled());

        BudgetConfig.setBudgetTrackingEnabled(false);
        assertFalse(BudgetConfig.isBudgetTrackingEnabled());

        BudgetConfig.setBudgetTrackingEnabled(true);
        assertTrue(BudgetConfig.isBudgetTrackingEnabled());
    }

    @Test
    @DisplayName("Test project name")
    public void testProjectName() {
        assertEquals("default", BudgetConfig.getProjectName());

        BudgetConfig.setProjectName("test_project");
        assertEquals("test_project", BudgetConfig.getProjectName());

        BudgetConfig.setProjectName("");
        assertEquals("test_project", BudgetConfig.getProjectName());

        BudgetConfig.setProjectName(null);
        assertEquals("test_project", BudgetConfig.getProjectName());
    }

    @Test
    @DisplayName("Test budget exceeded check")
    public void testWouldExceedGlobalBudget() {
        BudgetConfig.setMaxBudget(10.0);
        BudgetConfig.setCurrentCost(5.0);

        assertFalse(BudgetConfig.wouldExceedGlobalBudget(4.0));
        assertTrue(BudgetConfig.wouldExceedGlobalBudget(6.0));
        assertTrue(BudgetConfig.wouldExceedGlobalBudget(5.1));
    }

    @Test
    @DisplayName("Test budget exceeded check with null max budget")
    public void testWouldExceedGlobalBudgetWithNullMax() {
        BudgetConfig.setCurrentCost(5.0);
        assertFalse(BudgetConfig.wouldExceedGlobalBudget(100.0));
    }

    @Test
    @DisplayName("Test remaining global budget")
    public void testGetRemainingGlobalBudget() {
        BudgetConfig.setMaxBudget(10.0);
        BudgetConfig.setCurrentCost(3.0);
        assertEquals(7.0, BudgetConfig.getRemainingGlobalBudget());

        BudgetConfig.setCurrentCost(15.0);
        assertEquals(0.0, BudgetConfig.getRemainingGlobalBudget());
    }

    @Test
    @DisplayName("Test remaining global budget with null max")
    public void testGetRemainingGlobalBudgetWithNullMax() {
        BudgetConfig.setCurrentCost(5.0);
        assertNull(BudgetConfig.getRemainingGlobalBudget());
    }

    @Test
    @DisplayName("Test reset current cost")
    public void testResetCurrentCost() {
        BudgetConfig.setCurrentCost(10.0);
        assertEquals(10.0, BudgetConfig.getCurrentCost());

        BudgetConfig.resetCurrentCost();
        assertEquals(0.0, BudgetConfig.getCurrentCost());
    }

    @Test
    @DisplayName("Test clear all settings")
    public void testClear() {
        BudgetConfig.setMaxBudget(10.0);
        BudgetConfig.setCurrentCost(5.0);
        BudgetConfig.setProjectName("test");
        BudgetConfig.setBudgetTrackingEnabled(false);

        BudgetConfig.clear();

        assertNull(BudgetConfig.getMaxBudget());
        assertEquals(0.0, BudgetConfig.getCurrentCost());
        assertEquals("default", BudgetConfig.getProjectName());
        assertTrue(BudgetConfig.isBudgetTrackingEnabled());
    }

    @Test
    @DisplayName("Test toString")
    public void testToString() {
        BudgetConfig.setMaxBudget(10.0);
        BudgetConfig.setCurrentCost(5.0);
        BudgetConfig.setProjectName("test_project");
        BudgetConfig.setBudgetTrackingEnabled(true);

        assertEquals(10.0, BudgetConfig.getMaxBudget());
        assertEquals(5.0, BudgetConfig.getCurrentCost());
        assertEquals("test_project", BudgetConfig.getProjectName());
        assertTrue(BudgetConfig.isBudgetTrackingEnabled());
    }

    @Test
    @DisplayName("Test toString with null max budget")
    public void testToStringWithNullMaxBudget() {
        BudgetConfig.setCurrentCost(5.0);

        assertNull(BudgetConfig.getMaxBudget());
        assertEquals(5.0, BudgetConfig.getCurrentCost());
    }

    @Test
    @DisplayName("Test thread safety of addToCurrentCost")
    public void testThreadSafety() throws InterruptedException {
        BudgetConfig.setCurrentCost(0.0);

        int threadCount = 10;
        int incrementsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    BudgetConfig.addToCurrentCost(1.0);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * incrementsPerThread, BudgetConfig.getCurrentCost());
    }
}