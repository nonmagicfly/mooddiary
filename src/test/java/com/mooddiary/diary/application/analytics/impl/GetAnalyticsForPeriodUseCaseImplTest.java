package com.mooddiary.diary.application.analytics.impl;

import com.mooddiary.diary.application.analytics.AnalyticsCalculator;
import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.GetAnalyticsForPeriodUseCase;
import com.mooddiary.diary.application.analytics.MoodAnalyticsResponse;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;
import com.mooddiary.diary.application.port.out.DiaryEntryAnalyticsRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAnalyticsForPeriodUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryAnalyticsRepositoryPort repositoryPort;

    @Mock
    private AnalyticsCalculator calculator;

    @InjectMocks
    private GetAnalyticsForPeriodUseCaseImpl useCase;

    @Test
    void shouldExecuteHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 7);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(repositoryPort.findCompletedMetrics(userId, from, to)).thenReturn(List.of(
                new DiaryEntryAnalyticsMetric(2, 2, 2, 2, 2)
        ));
        when(repositoryPort.findCompletedTagFrequencies(userId, from, to)).thenReturn(List.of(
                new TagFrequencyAnalytics(UUID.randomUUID(), "work", "red", 3)
        ));
        when(repositoryPort.findCompletedSeries(userId, from, to)).thenReturn(List.of());

        MoodAnalyticsResponse expected = new MoodAnalyticsResponse(
                from,
                to,
                2.0,
                2.0,
                2.0,
                1,
                List.of(),
                new com.mooddiary.diary.application.analytics.AnalyticsCorrelations(null),
                List.of()
        );
        when(calculator.calculate(eq(from), eq(to), any(), any(), any())).thenReturn(expected);

        MoodAnalyticsResponse response = useCase.execute(keycloakSubject, from, to);
        assertEquals(expected, response);
    }

    @Test
    void shouldThrowWhenToBeforeFrom() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("sub-1", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 1)));
    }
}

