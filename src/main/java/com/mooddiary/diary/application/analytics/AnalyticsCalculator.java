package com.mooddiary.diary.application.analytics;

import java.util.List;
import java.util.UUID;

public interface AnalyticsCalculator {
    MoodAnalyticsResponse calculate(
            java.time.LocalDate periodStart,
            java.time.LocalDate periodEnd,
            List<DiaryEntryAnalyticsMetric> metrics,
            List<TagFrequencyAnalytics> tagFrequencies,
            List<DiaryEntrySeriesPoint> series
    );
}

