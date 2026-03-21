package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import com.mooddiary.diary.domain.diary.Score1to10;
import com.mooddiary.diary.infrastructure.persistence.jpa.DiaryEntryJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.DiaryEntryJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryRepositoryJpaAdapterTest {
    @Mock
    private DiaryEntryJpaRepository diaryEntryJpaRepository;

    @Mock
    private EntityManager entityManager;

    @Test
    void shouldSaveDiaryEntryMapping() {
        DiaryEntryRepositoryPort adapter = new DiaryEntryRepositoryJpaAdapter(diaryEntryJpaRepository, entityManager);

        UUID diaryEntryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();
        UUID tagId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();

        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        TagJpaEntity tagRef = new TagJpaEntity();
        tagRef.setId(tagId);
        SymptomJpaEntity symptomRef = new SymptomJpaEntity();
        symptomRef.setId(symptomId);

        when(entityManager.getReference(TagJpaEntity.class, tagId)).thenReturn(tagRef);
        when(entityManager.getReference(SymptomJpaEntity.class, symptomId)).thenReturn(symptomRef);

        DiaryEntry domainEntry = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                entryDate,
                Score1to10.of(1),
                Score1to10.of(2),
                Score1to10.of(3),
                Score1to10.of(4),
                Score1to10.of(5),
                "note",
                true,
                Set.of(tagId),
                Set.of(symptomId),
                createdAt,
                updatedAt
        );

        DiaryEntryJpaEntity savedEntity = new DiaryEntryJpaEntity();
        savedEntity.setId(diaryEntryId);
        savedEntity.setUserId(userId);
        savedEntity.setEntryDate(entryDate);
        savedEntity.setMoodScore((short) 1);
        savedEntity.setEnergyScore((short) 2);
        savedEntity.setProductivityScore((short) 3);
        savedEntity.setStressScore((short) 4);
        savedEntity.setSleepQualityScore((short) 5);
        savedEntity.setNote("note");
        savedEntity.setCompleted(true);
        savedEntity.setCreatedAt(createdAt);
        savedEntity.setUpdatedAt(updatedAt);
        savedEntity.setTags(Set.of(tagRef));
        savedEntity.setSymptoms(Set.of(symptomRef));

        when(diaryEntryJpaRepository.save(any(DiaryEntryJpaEntity.class))).thenReturn(savedEntity);

        DiaryEntry result = adapter.save(domainEntry);

        assertEquals(diaryEntryId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(entryDate, result.getEntryDate());
        assertEquals(1, result.getMoodScore().value());
        assertEquals(2, result.getEnergyScore().value());
        assertEquals(3, result.getProductivityScore().value());
        assertEquals(4, result.getStressScore().value());
        assertEquals(5, result.getSleepQualityScore().value());
        assertEquals("note", result.getNote());
        assertEquals(true, result.isCompleted());
        assertEquals(Set.of(tagId), result.getTagIds());
        assertEquals(Set.of(symptomId), result.getSymptomIds());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
}

