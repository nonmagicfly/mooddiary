package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.analytics.DiaryEntryAnalyticsMetric;
import com.mooddiary.diary.application.analytics.TagFrequencyAnalytics;
import com.mooddiary.diary.application.port.out.DiaryEntryAnalyticsRepositoryPort;
import com.mooddiary.diary.infrastructure.persistence.jpa.DiaryEntryJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.analytics.DiaryEntryAnalyticsMetricRow;
import com.mooddiary.diary.infrastructure.persistence.jpa.analytics.DiaryEntrySeriesPointRow;
import com.mooddiary.diary.infrastructure.persistence.jpa.analytics.TagFrequencyAnalyticsRow;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class DiaryEntryAnalyticsRepositoryJpaAdapter implements DiaryEntryAnalyticsRepositoryPort {
    private final EntityManager entityManager;

    public DiaryEntryAnalyticsRepositoryJpaAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntryAnalyticsMetric> findCompletedMetrics(UUID userId, LocalDate from, LocalDate to) {
        List<DiaryEntryAnalyticsMetricRow> rows = entityManager.createQuery(
                        """
                        select new com.mooddiary.diary.infrastructure.persistence.jpa.analytics.DiaryEntryAnalyticsMetricRow(
                          de.moodScore,
                          de.energyScore,
                          de.productivityScore,
                          de.stressScore,
                          de.sleepQualityScore
                        )
                        from DiaryEntryJpaEntity de
                        where de.userId = :userId
                          and de.entryDate >= :from
                          and de.entryDate <= :to
                        """,
                        DiaryEntryAnalyticsMetricRow.class
                )
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return rows.stream().map(this::mapMetric).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagFrequencyAnalytics> findCompletedTagFrequencies(UUID userId, LocalDate from, LocalDate to) {
        List<TagFrequencyAnalyticsRow> rows = entityManager.createQuery(
                        """
                        select new com.mooddiary.diary.infrastructure.persistence.jpa.analytics.TagFrequencyAnalyticsRow(
                          t.id,
                          t.name,
                          t.color,
                          count(distinct de.id)
                        )
                        from DiaryEntryJpaEntity de
                        join de.tags t
                        where de.userId = :userId
                          and de.entryDate >= :from
                          and de.entryDate <= :to
                        group by t.id, t.name, t.color
                        order by count(distinct de.id) desc
                        """,
                        TagFrequencyAnalyticsRow.class
                )
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return rows.stream().map(this::mapTagFrequency).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint> findCompletedSeries(UUID userId, LocalDate from, LocalDate to) {
        List<DiaryEntrySeriesPointRow> rows = entityManager.createQuery(
                        """
                        select new com.mooddiary.diary.infrastructure.persistence.jpa.analytics.DiaryEntrySeriesPointRow(
                          de.entryDate,
                          de.moodScore,
                          de.energyScore,
                          de.productivityScore,
                          de.stressScore,
                          de.sleepQualityScore
                        )
                        from DiaryEntryJpaEntity de
                        where de.userId = :userId
                          and de.entryDate >= :from
                          and de.entryDate <= :to
                        order by de.entryDate asc
                        """,
                        DiaryEntrySeriesPointRow.class
                )
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return rows.stream().map(this::mapSeriesPoint).toList();
    }

    private DiaryEntryAnalyticsMetric mapMetric(DiaryEntryAnalyticsMetricRow row) {
        return new DiaryEntryAnalyticsMetric(
                row.getMoodScore(),
                row.getEnergyScore(),
                row.getProductivityScore(),
                row.getStressScore(),
                row.getSleepQualityScore()
        );
    }

    private TagFrequencyAnalytics mapTagFrequency(TagFrequencyAnalyticsRow row) {
        return new TagFrequencyAnalytics(
                row.getTagId(),
                row.getTagName(),
                row.getTagColor(),
                row.getCount()
        );
    }

    private com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint mapSeriesPoint(DiaryEntrySeriesPointRow row) {
        return new com.mooddiary.diary.application.analytics.DiaryEntrySeriesPoint(
                row.getEntryDate(),
                row.getMoodScore(),
                row.getEnergyScore(),
                row.getProductivityScore(),
                row.getStressScore(),
                row.getSleepQualityScore()
        );
    }
}

