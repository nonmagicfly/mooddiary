package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;
import com.mooddiary.diary.application.port.out.DiaryEntryAnalyticsRepositoryPort;
import com.mooddiary.diary.infrastructure.persistence.jpa.analytics.DiaryEntryAnalyticsMetricRow;
import com.mooddiary.diary.infrastructure.persistence.jpa.analytics.TagFrequencyAnalyticsRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryAnalyticsRepositoryJpaAdapterTest {
    @Mock
    private EntityManager entityManager;

    @Test
    void shouldMapMetricsAndTagFrequencies() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 7);
        UUID userId = UUID.randomUUID();

        TypedQuery<DiaryEntryAnalyticsMetricRow> metricsQuery = org.mockito.Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(DiaryEntryAnalyticsMetricRow.class)
        )).thenReturn(metricsQuery);

        when(metricsQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(metricsQuery);
        when(metricsQuery.getResultList()).thenReturn(List.of(new DiaryEntryAnalyticsMetricRow(1, 2, 3, 4, 5)));

        TypedQuery<TagFrequencyAnalyticsRow> tagsQuery = org.mockito.Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(TagFrequencyAnalyticsRow.class)
        )).thenReturn(tagsQuery);

        when(tagsQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(tagsQuery);
        UUID tagId = UUID.randomUUID();
        when(tagsQuery.getResultList()).thenReturn(List.of(new TagFrequencyAnalyticsRow(tagId, "work", "red", 2L)));

        DiaryEntryAnalyticsRepositoryPort adapter = new DiaryEntryAnalyticsRepositoryJpaAdapter(entityManager);

        List<DiaryEntryAnalyticsMetric> metrics = adapter.findCompletedMetrics(userId, from, to);
        assertEquals(1, metrics.size());
        assertEquals(1, metrics.get(0).moodScore());
        assertEquals(5, metrics.get(0).sleepQualityScore());

        List<TagFrequencyAnalytics> tags = adapter.findCompletedTagFrequencies(userId, from, to);
        assertEquals(1, tags.size());
        assertEquals(tagId, tags.get(0).tagId());
        assertEquals(2L, tags.get(0).count());
    }
}

