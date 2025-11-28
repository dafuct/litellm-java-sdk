package com.litellm.sdk.unit.budget;

import com.litellm.sdk.model.budget.BudgetDuration;
import com.litellm.sdk.model.budget.UserBudget;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserBudgetTest {

    @Test
    @DisplayName("Test create user budget")
    public void testCreate() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);

        assertEquals("user123", budget.userId());
        assertEquals(100.0, budget.totalBudget());
        assertEquals(BudgetDuration.MONTHLY, budget.duration());
        assertEquals(0.0, budget.currentCost());
        assertTrue(budget.createdAt() > 0);
        assertTrue(budget.resetAt() > budget.createdAt());
        assertNotNull(budget.modelCosts());
        assertTrue(budget.modelCosts().isEmpty());
    }

    @Test
    @DisplayName("Test should reset")
    public void testShouldReset() {
        UserBudget budget = new UserBudget(
            "user123",
            100.0,
            BudgetDuration.DAILY,
            1000000000000L,
            1000000001000L,
            0.0,
            java.util.Map.of()
        );

        assertTrue(budget.shouldReset(1000000002000L));
    }

    @Test
    @DisplayName("Test should not reset when time hasn't come")
    public void testShouldNotReset() {
        long now = System.currentTimeMillis();
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.DAILY);

        assertFalse(budget.shouldReset(now));
    }

    @Test
    @DisplayName("Test should not reset for NONE duration")
    public void testShouldNotResetForNone() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.NONE);
        long farFuture = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000;

        assertFalse(budget.shouldReset(farFuture));
    }

    @Test
    @DisplayName("Test would exceed budget")
    public void testWouldExceedBudget() {
        UserBudget budget = new UserBudget(
            "user123",
            100.0,
            BudgetDuration.MONTHLY,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            50.0,
            java.util.Map.of()
        );

        assertFalse(budget.wouldExceedBudget(49.0));
        assertTrue(budget.wouldExceedBudget(50.1));
        assertTrue(budget.wouldExceedBudget(50.0));
    }

    @Test
    @DisplayName("Test get remaining budget")
    public void testGetRemainingBudget() {
        UserBudget budget = new UserBudget(
            "user123",
            100.0,
            BudgetDuration.MONTHLY,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            30.0,
            java.util.Map.of()
        );

        assertEquals(70.0, budget.getRemainingBudget());
    }

    @Test
    @DisplayName("Test get remaining budget when exceeded")
    public void testGetRemainingBudgetWhenExceeded() {
        UserBudget budget = new UserBudget(
            "user123",
            100.0,
            BudgetDuration.MONTHLY,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            150.0,
            java.util.Map.of()
        );

        assertEquals(0.0, budget.getRemainingBudget());
    }

    @Test
    @DisplayName("Test get model cost")
    public void testGetModelCost() {
        UserBudget budget = new UserBudget(
            "user123",
            100.0,
            BudgetDuration.MONTHLY,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            30.0,
            java.util.Map.of("gpt-4", 20.0, "gpt-3.5-turbo", 10.0)
        );

        assertEquals(20.0, budget.getModelCost("gpt-4"));
        assertEquals(10.0, budget.getModelCost("gpt-3.5-turbo"));
        assertEquals(0.0, budget.getModelCost("unknown-model"));
    }

    @Test
    @DisplayName("Test update cost")
    public void testUpdateCost() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        long now = System.currentTimeMillis();

        UserBudget updated = budget.updateCost(25.0, "gpt-4", now);

        assertEquals(25.0, updated.currentCost());
        assertEquals(25.0, updated.getModelCost("gpt-4"));
        assertEquals(0.0, budget.currentCost());
    }

    @Test
    @DisplayName("Test update cost accumulates model costs")
    public void testUpdateCostAccumulates() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        long now = System.currentTimeMillis();

        UserBudget updated1 = budget.updateCost(10.0, "gpt-4", now);
        UserBudget updated2 = updated1.updateCost(15.0, "gpt-4", now);

        assertEquals(25.0, updated2.getModelCost("gpt-4"));
        assertEquals(25.0, updated2.currentCost());
    }

    @Test
    @DisplayName("Test reset cost")
    public void testResetCost() {
        UserBudget budget = new UserBudget(
            "user123",
            100.0,
            BudgetDuration.DAILY,
            1000000000000L,
            1000000001000L,
            50.0,
            java.util.Map.of("gpt-4", 30.0, "gpt-3.5-turbo", 20.0)
        );

        long now = 1000000002000L;
        UserBudget reset = budget.resetCost(now);

        assertEquals(0.0, reset.currentCost());
        assertTrue(reset.modelCosts().isEmpty());
        assertTrue(reset.resetAt() > budget.resetAt());
    }

    @Test
    @DisplayName("Test reset cost does not reset when not needed")
    public void testResetCostDoesNotResetWhenNotNeeded() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.DAILY);
        long originalResetAt = budget.resetAt();
        long now = System.currentTimeMillis();

        UserBudget reset = budget.resetCost(now);

        assertEquals(budget.currentCost(), reset.currentCost());
        assertEquals(budget.modelCosts(), reset.modelCosts());
        assertEquals(originalResetAt, reset.resetAt());
    }

    @Test
    @DisplayName("Test update total budget")
    public void testUpdateTotalBudget() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);
        long now = System.currentTimeMillis();
        UserBudget withCost = budget.updateCost(30.0, "gpt-4", now);

        UserBudget updated = withCost.updateTotalBudget(200.0);

        assertEquals(200.0, updated.totalBudget());
        assertEquals(30.0, updated.currentCost());
    }

    @Test
    @DisplayName("Test update duration")
    public void testUpdateDuration() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.DAILY);
        long now = System.currentTimeMillis();

        UserBudget updated = budget.updateDuration(BudgetDuration.MONTHLY, now);

        assertEquals(BudgetDuration.MONTHLY, updated.duration());
        assertTrue(updated.resetAt() > budget.resetAt());
    }

    @Test
    @DisplayName("Test toString")
    public void testToString() {
        UserBudget budget = UserBudget.create("user123", 100.0, BudgetDuration.MONTHLY);

        String result = budget.toString();
        assertNotNull(result);
        assertTrue(result.contains("userId='user123'"));
        assertTrue(result.contains("totalBudget=100.0"));
        assertTrue(result.contains("duration=MONTHLY"));
        assertTrue(result.contains("currentCost=0.0"));
        assertTrue(result.contains("remainingBudget="));
    }
}