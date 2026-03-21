package com.mooddiary.diary.application.analytics;

public record DiaryEntryAnalyticsMetric(
        int moodScore,
        int energyScore,
        int productivityScore,
        int stressScore,
        int sleepQualityScore
) {
}

