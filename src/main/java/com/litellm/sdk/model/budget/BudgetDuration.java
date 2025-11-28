package com.litellm.sdk.model.budget;

public enum BudgetDuration {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    NONE;

    public long getNextResetTime(long currentTimeMs) {
        switch (this) {
            case DAILY:
                return getNextDay(currentTimeMs);
            case WEEKLY:
                return getNextWeek(currentTimeMs);
            case MONTHLY:
                return getNextMonth(currentTimeMs);
            case YEARLY:
                return getNextYear(currentTimeMs);
            case NONE:
            default:
                return Long.MAX_VALUE;
        }
    }

    private long getNextDay(long currentTimeMs) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(currentTimeMs);
        java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(
            instant, java.time.ZoneId.systemDefault()
        ).plusDays(1).toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    private long getNextWeek(long currentTimeMs) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(currentTimeMs);
        java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(
            instant, java.time.ZoneId.systemDefault()
        );
        int daysUntilNextMonday = (8 - zdt.getDayOfWeek().getValue()) % 7;
        if (daysUntilNextMonday == 0) daysUntilNextMonday = 7;
        return zdt.plusDays(daysUntilNextMonday)
            .toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }

    private long getNextMonth(long currentTimeMs) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(currentTimeMs);
        java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(
            instant, java.time.ZoneId.systemDefault()
        ).plusMonths(1).toLocalDate().withDayOfMonth(1)
            .atStartOfDay(java.time.ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    private long getNextYear(long currentTimeMs) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(currentTimeMs);
        java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(
            instant, java.time.ZoneId.systemDefault()
        ).plusYears(1).toLocalDate().withMonth(1).withDayOfMonth(1)
            .atStartOfDay(java.time.ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
}
