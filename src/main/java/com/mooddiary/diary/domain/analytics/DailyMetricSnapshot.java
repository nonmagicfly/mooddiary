package com.mooddiary.diary.domain.analytics;

import com.mooddiary.diary.domain.diary.Score1to10;

import java.time.LocalDate;
import java.util.UUID;

public final class DailyMetricSnapshot {
    private final UUID userId;
    private final LocalDate entryDate;
    private final Score1to10 moodScore;
    private final Score1to10 energyScore;
    private final Score1to10 productivityScore;
    private final Score1to10 stressScore;
    private final Score1to10 sleepQualityScore;
    private final boolean isCompleted;

    public DailyMetricSnapshot(
            UUID userId,
            LocalDate entryDate,
            Score1to10 moodScore,
            Score1to10 energyScore,
            Score1to10 productivityScore,
            Score1to10 stressScore,
            Score1to10 sleepQualityScore,
            boolean isCompleted
    ) {
        this.userId = userId;
        this.entryDate = entryDate;
        this.moodScore = moodScore;
        this.energyScore = energyScore;
        this.productivityScore = productivityScore;
        this.stressScore = stressScore;
        this.sleepQualityScore = sleepQualityScore;
        this.isCompleted = isCompleted;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public Score1to10 getMoodScore() {
        return moodScore;
    }

    public Score1to10 getEnergyScore() {
        return energyScore;
    }

    public Score1to10 getProductivityScore() {
        return productivityScore;
    }

    public Score1to10 getStressScore() {
        return stressScore;
    }

    public Score1to10 getSleepQualityScore() {
        return sleepQualityScore;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}

