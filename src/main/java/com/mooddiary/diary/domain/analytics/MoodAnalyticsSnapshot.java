package com.mooddiary.diary.domain.analytics;

import java.time.LocalDate;
import java.util.UUID;

public final class MoodAnalyticsSnapshot {
    private final UUID userId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final Double avgMoodScore;
    private final Double avgEnergyScore;
    private final Double avgProductivityScore;
    private final long completedDaysCount;

    public MoodAnalyticsSnapshot(
            UUID userId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Double avgMoodScore,
            Double avgEnergyScore,
            Double avgProductivityScore,
            long completedDaysCount
    ) {
        this.userId = userId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.avgMoodScore = avgMoodScore;
        this.avgEnergyScore = avgEnergyScore;
        this.avgProductivityScore = avgProductivityScore;
        this.completedDaysCount = completedDaysCount;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public Double getAvgMoodScore() {
        return avgMoodScore;
    }

    public Double getAvgEnergyScore() {
        return avgEnergyScore;
    }

    public Double getAvgProductivityScore() {
        return avgProductivityScore;
    }

    public long getCompletedDaysCount() {
        return completedDaysCount;
    }
}

