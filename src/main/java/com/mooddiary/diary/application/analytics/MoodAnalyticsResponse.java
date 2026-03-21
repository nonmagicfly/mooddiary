package com.mooddiary.diary.application.analytics;

import java.time.LocalDate;
import java.util.List;

public record MoodAnalyticsResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        Double avgMoodScore,
        Double avgEnergyScore,
        Double avgProductivityScore,
        long completedDaysCount,
        List<TagFrequencyAnalytics> tagFrequencies,
        AnalyticsCorrelations correlations,
        List<DiaryEntrySeriesPoint> series
) {
}

