package com.mooddiary.diary.application.analytics.impl;

import com.mooddiary.diary.application.analytics.AnalyticsCalculator;
import com.mooddiary.diary.application.analytics.AnalyticsCorrelations;
import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint;
import com.mooddiary.diary.application.analytics.MoodAnalyticsResponse;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AnalyticsCalculatorImplTest {
    private final AnalyticsCalculator calculator = new AnalyticsCalculatorImpl();

    @Test
    void shouldCalculateAveragesAndPerfectCorrelations() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 7);

        List<DiaryEntryAnalyticsMetric> metrics = List.of(
                new DiaryEntryAnalyticsMetric(2, 1, 2, 1, 1),
                new DiaryEntryAnalyticsMetric(4, 2, 4, 2, 2),
                new DiaryEntryAnalyticsMetric(6, 3, 6, 3, 3)
        );

        List<TagFrequencyAnalytics> tags = List.of(
                new TagFrequencyAnalytics(UUID.randomUUID(), "work", "red", 2)
        );

        List<DiaryEntrySeriesPoint> series = List.of(
                new DiaryEntrySeriesPoint(from.plusDays(0), 2, 1, 2, 1, 1),
                new DiaryEntrySeriesPoint(from.plusDays(1), 4, 2, 4, 2, 2),
                new DiaryEntrySeriesPoint(from.plusDays(2), 6, 3, 6, 3, 3)
        );

        MoodAnalyticsResponse response = calculator.calculate(from, to, metrics, tags, series);

        assertEquals(3, response.completedDaysCount());
        assertEquals(4.0, response.avgMoodScore());
        assertEquals(2.0, response.avgEnergyScore());
        assertEquals(4.0, response.avgProductivityScore());

        AnalyticsCorrelations correlations = response.correlations();
        assertNull(correlations.sleepToMood());
        assertNull(correlations.sleepToEnergy());
        assertEquals(1.0, correlations.stressToProductivity(), 1e-9);
    }

    @Test
    void shouldReturnNullCorrelationsWhenVarianceIsZero() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 1);

        List<DiaryEntryAnalyticsMetric> metrics = List.of(
                new DiaryEntryAnalyticsMetric(2, 1, 2, 1, 1),
                new DiaryEntryAnalyticsMetric(4, 2, 4, 1, 1),
                new DiaryEntryAnalyticsMetric(6, 3, 6, 1, 1)
        );

        MoodAnalyticsResponse response = calculator.calculate(from, to, metrics, List.of(), List.of());

        assertNull(response.correlations().sleepToMood());
        assertNull(response.correlations().sleepToEnergy());
        assertNull(response.correlations().stressToProductivity());
    }
}

