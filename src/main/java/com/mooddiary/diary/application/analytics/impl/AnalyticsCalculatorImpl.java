package com.mooddiary.diary.application.analytics.impl;

import com.mooddiary.diary.application.analytics.AnalyticsCalculator;
import com.mooddiary.diary.application.analytics.AnalyticsCorrelations;
import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint;
import com.mooddiary.diary.application.analytics.MoodAnalyticsResponse;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsCalculatorImpl implements AnalyticsCalculator {
    @Override
    public MoodAnalyticsResponse calculate(
            LocalDate periodStart,
            LocalDate periodEnd,
            List<DiaryEntryAnalyticsMetric> metrics,
            List<TagFrequencyAnalytics> tagFrequencies,
            List<DiaryEntrySeriesPoint> series
    ) {
        long completedDaysCount = metrics == null ? 0 : metrics.size();

        Double avgMood = completedDaysCount == 0 ? null : average(metrics, DiaryEntryAnalyticsMetric::moodScore);
        Double avgEnergy = completedDaysCount == 0 ? null : average(metrics, DiaryEntryAnalyticsMetric::energyScore);
        Double avgProductivity = completedDaysCount == 0 ? null : average(metrics, DiaryEntryAnalyticsMetric::productivityScore);

        AnalyticsCorrelations correlations = completedDaysCount < 2
                ? new AnalyticsCorrelations(null, null, null)
                : new AnalyticsCorrelations(
                null,
                null,
                pearson(metrics, DiaryEntryAnalyticsMetric::stressScore, DiaryEntryAnalyticsMetric::productivityScore)
        );

        List<TagFrequencyAnalytics> safeTagFrequencies = tagFrequencies == null ? List.of() : tagFrequencies;
        List<DiaryEntrySeriesPoint> safeSeries = series == null ? List.of() : series;

        return new MoodAnalyticsResponse(
                periodStart,
                periodEnd,
                avgMood,
                avgEnergy,
                avgProductivity,
                completedDaysCount,
                safeTagFrequencies,
                correlations,
                safeSeries
        );
    }

    private double average(List<DiaryEntryAnalyticsMetric> metrics, java.util.function.ToIntFunction<DiaryEntryAnalyticsMetric> extractor) {
        long sum = 0;
        for (DiaryEntryAnalyticsMetric m : metrics) {
            sum += extractor.applyAsInt(m);
        }
        return (double) sum / (double) metrics.size();
    }

    private Double pearson(
            List<DiaryEntryAnalyticsMetric> metrics,
            java.util.function.ToIntFunction<DiaryEntryAnalyticsMetric> xExtractor,
            java.util.function.ToIntFunction<DiaryEntryAnalyticsMetric> yExtractor
    ) {
        if (metrics.size() < 2) return null;

        int n = metrics.size();
        double sumX = 0;
        double sumY = 0;
        for (DiaryEntryAnalyticsMetric m : metrics) {
            sumX += xExtractor.applyAsInt(m);
            sumY += yExtractor.applyAsInt(m);
        }
        double meanX = sumX / n;
        double meanY = sumY / n;

        double sxx = 0;
        double syy = 0;
        double sxy = 0;
        for (DiaryEntryAnalyticsMetric m : metrics) {
            double dx = xExtractor.applyAsInt(m) - meanX;
            double dy = yExtractor.applyAsInt(m) - meanY;
            sxx += dx * dx;
            syy += dy * dy;
            sxy += dx * dy;
        }

        double denom = Math.sqrt(sxx * syy);
        if (denom == 0) return null;
        return sxy / denom;
    }
}

