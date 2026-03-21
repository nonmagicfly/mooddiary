package com.mooddiary.diary.application.port.out;

import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DiaryEntryAnalyticsRepositoryPort {

    List<DiaryEntryAnalyticsMetric> findCompletedMetrics(UUID userId, LocalDate from, LocalDate to);

    List<TagFrequencyAnalytics> findCompletedTagFrequencies(UUID userId, LocalDate from, LocalDate to);

    List<DiaryEntrySeriesPoint> findCompletedSeries(UUID userId, LocalDate from, LocalDate to);
}
