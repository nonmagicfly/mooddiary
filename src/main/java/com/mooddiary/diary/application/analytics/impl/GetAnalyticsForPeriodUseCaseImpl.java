package com.mooddiary.diary.application.analytics.impl;

import com.mooddiary.diary.application.analytics.AnalyticsCalculator;
import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.GetAnalyticsForPeriodUseCase;
import com.mooddiary.diary.application.analytics.MoodAnalyticsResponse;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;
import com.mooddiary.diary.application.port.out.DiaryEntryAnalyticsRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GetAnalyticsForPeriodUseCaseImpl implements GetAnalyticsForPeriodUseCase {
    private final UserIdentityService userIdentityService;
    private final DiaryEntryAnalyticsRepositoryPort repositoryPort;
    private final AnalyticsCalculator calculator;

    public GetAnalyticsForPeriodUseCaseImpl(
            UserIdentityService userIdentityService,
            DiaryEntryAnalyticsRepositoryPort repositoryPort,
            AnalyticsCalculator calculator
    ) {
        this.userIdentityService = userIdentityService;
        this.repositoryPort = repositoryPort;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public MoodAnalyticsResponse execute(String keycloakSubject, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must be >= from");
        }

        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);

        List<DiaryEntryAnalyticsMetric> metrics = repositoryPort.findCompletedMetrics(userId, from, to);
        List<TagFrequencyAnalytics> tagFrequencies = repositoryPort.findCompletedTagFrequencies(userId, from, to);
        List<com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint> series = repositoryPort.findCompletedSeries(userId, from, to);

        return calculator.calculate(from, to, metrics, tagFrequencies, series);
    }
}

