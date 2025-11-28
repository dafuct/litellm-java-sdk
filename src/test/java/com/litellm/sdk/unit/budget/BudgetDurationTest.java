package com.litellm.sdk.unit.budget;

import com.litellm.sdk.model.budget.BudgetDuration;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BudgetDurationTest {

    @Test
    @DisplayName("Test all duration values exist")
    public void testDurationValues() {
        BudgetDuration[] durations = BudgetDuration.values();
        assertEquals(5, durations.length);
        assertTrue(contains(durations, BudgetDuration.DAILY));
        assertTrue(contains(durations, BudgetDuration.WEEKLY));
        assertTrue(contains(durations, BudgetDuration.MONTHLY));
        assertTrue(contains(durations, BudgetDuration.YEARLY));
        assertTrue(contains(durations, BudgetDuration.NONE));
    }

    @Test
    @DisplayName("Test daily reset time calculation")
    public void testDailyResetTime() {
        long now = 1704067200000L;
        long nextReset = BudgetDuration.DAILY.getNextResetTime(now);

        assertTrue(nextReset > now);
        assertTrue(nextReset - now < 25 * 60 * 60 * 1000);
        assertTrue(nextReset - now > 23 * 60 * 60 * 1000);
    }

    @Test
    @DisplayName("Test weekly reset time calculation")
    public void testWeeklyResetTime() {
        long now = 1704067200000L;
        long nextReset = BudgetDuration.WEEKLY.getNextResetTime(now);

        assertTrue(nextReset > now);
        assertTrue(nextReset - now >= 6 * 24 * 60 * 60 * 1000);
        assertTrue(nextReset - now <= 8 * 24 * 60 * 60 * 1000);
    }

    @Test
    @DisplayName("Test monthly reset time calculation")
    public void testMonthlyResetTime() {
        long now = 1704067200000L;
        long nextReset = BudgetDuration.MONTHLY.getNextResetTime(now);

        assertTrue(nextReset > now);
        long oneMonthMs = 30L * 24 * 60 * 60 * 1000;
        assertTrue(nextReset - now >= oneMonthMs - 2 * 24 * 60 * 60 * 1000);
        assertTrue(nextReset - now <= oneMonthMs + 2 * 24 * 60 * 60 * 1000);
    }

    @Test
    @DisplayName("Test yearly reset time calculation")
    public void testYearlyResetTime() {
        long now = 1704067200000L;
        long nextReset = BudgetDuration.YEARLY.getNextResetTime(now);

        assertTrue(nextReset > now);
        assertTrue(nextReset - now >= 365 * 24 * 60 * 60 * 1000L);
        assertTrue(nextReset - now <= 366 * 24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("Test none reset time")
    public void testNoneResetTime() {
        long now = 1704067200000L;
        long nextReset = BudgetDuration.NONE.getNextResetTime(now);

        assertEquals(Long.MAX_VALUE, nextReset);
    }

    private boolean contains(BudgetDuration[] durations, BudgetDuration duration) {
        for (BudgetDuration d : durations) {
            if (d == duration) {
                return true;
            }
        }
        return false;
    }
}