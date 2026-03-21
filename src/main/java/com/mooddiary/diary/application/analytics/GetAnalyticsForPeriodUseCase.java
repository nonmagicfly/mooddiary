package com.mooddiary.diary.application.analytics;

public interface GetAnalyticsForPeriodUseCase {
    MoodAnalyticsResponse execute(String keycloakSubject, java.time.LocalDate from, java.time.LocalDate to);
}

