package com.mooddiary.diary.application.analytics;

import java.time.LocalDate;

public record DiaryEntrySeriesPoint(
        LocalDate entryDate,
        int moodScore,
        int energyScore,
        int productivityScore,
        int stressScore,
        int sleepQualityScore
) {
}

