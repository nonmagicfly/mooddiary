package com.mooddiary.diary.infrastructure.persistence.jpa.analytics;

public class DiaryEntryAnalyticsMetricRow {
    private final int moodScore;
    private final int energyScore;
    private final int productivityScore;
    private final int stressScore;
    private final int sleepQualityScore;

    public DiaryEntryAnalyticsMetricRow(
            int moodScore,
            int energyScore,
            int productivityScore,
            int stressScore,
            int sleepQualityScore
    ) {
        this.moodScore = moodScore;
        this.energyScore = energyScore;
        this.productivityScore = productivityScore;
        this.stressScore = stressScore;
        this.sleepQualityScore = sleepQualityScore;
    }

    public int getMoodScore() {
        return moodScore;
    }

    public int getEnergyScore() {
        return energyScore;
    }

    public int getProductivityScore() {
        return productivityScore;
    }

    public int getStressScore() {
        return stressScore;
    }

    public int getSleepQualityScore() {
        return sleepQualityScore;
    }
}

