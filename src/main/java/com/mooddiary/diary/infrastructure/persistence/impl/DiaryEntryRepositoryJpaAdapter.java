package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import com.mooddiary.diary.domain.diary.Score1to10;
import com.mooddiary.diary.infrastructure.persistence.jpa.DiaryEntryJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.DiaryEntryJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class DiaryEntryRepositoryJpaAdapter implements DiaryEntryRepositoryPort {
    private final DiaryEntryJpaRepository diaryEntryJpaRepository;
    private final EntityManager entityManager;

    public DiaryEntryRepositoryJpaAdapter(DiaryEntryJpaRepository diaryEntryJpaRepository, EntityManager entityManager) {
        this.diaryEntryJpaRepository = diaryEntryJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndEntryDate(UUID userId, LocalDate entryDate) {
        return diaryEntryJpaRepository.existsByUserIdAndEntryDate(userId, entryDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiaryEntry> findByIdAndUserId(UUID diaryEntryId, UUID userId) {
        return diaryEntryJpaRepository.findByIdAndUserId(diaryEntryId, userId)
                .map(this::mapToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntry> findByUserId(UUID userId, LocalDate from, LocalDate to, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "entryDate"));
        if (from == null && to == null) {
            return diaryEntryJpaRepository.findByUserIdOrderByEntryDateDesc(userId, pageable)
                    .stream().map(this::mapToDomain).toList();
        }
        if (from != null && to != null) {
            return diaryEntryJpaRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(userId, from, to, pageable)
                    .stream().map(this::mapToDomain).toList();
        }
        if (from != null) {
            return diaryEntryJpaRepository.findByUserIdAndEntryDateGreaterThanEqualOrderByEntryDateDesc(userId, from, pageable)
                    .stream().map(this::mapToDomain).toList();
        }
        return diaryEntryJpaRepository.findByUserIdAndEntryDateLessThanEqualOrderByEntryDateDesc(userId, to, pageable)
                .stream().map(this::mapToDomain).toList();
    }

    @Override
    @Transactional
    public DiaryEntry save(DiaryEntry entry) {
        DiaryEntryJpaEntity entity = mapToEntity(entry);
        DiaryEntryJpaEntity saved = diaryEntryJpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    @Transactional
    public boolean deleteByIdAndUserId(UUID diaryEntryId, UUID userId) {
        return diaryEntryJpaRepository.deleteByIdAndUserId(diaryEntryId, userId) > 0;
    }

    private DiaryEntry mapToDomain(DiaryEntryJpaEntity entity) {
        Set<UUID> tagIds = entity.getTags().stream().map(TagJpaEntity::getId).collect(java.util.stream.Collectors.toSet());
        Set<UUID> symptomIds = entity.getSymptoms().stream().map(SymptomJpaEntity::getId).collect(java.util.stream.Collectors.toSet());

        return DiaryEntry.fromPersistence(
                entity.getId(),
                entity.getUserId(),
                entity.getEntryDate(),
                Score1to10.of(entity.getMoodScore()),
                Score1to10.of(entity.getEnergyScore()),
                Score1to10.of(entity.getProductivityScore()),
                Score1to10.of(entity.getStressScore()),
                Score1to10.of(entity.getSleepQualityScore()),
                entity.getNote(),
                entity.isCompleted(),
                tagIds,
                symptomIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private DiaryEntryJpaEntity mapToEntity(DiaryEntry entry) {
        DiaryEntryJpaEntity entity = new DiaryEntryJpaEntity();
        entity.setId(entry.getId());
        entity.setUserId(entry.getUserId());
        entity.setEntryDate(entry.getEntryDate());
        entity.setMoodScore((short) entry.getMoodScore().value());
        entity.setEnergyScore((short) entry.getEnergyScore().value());
        entity.setProductivityScore((short) entry.getProductivityScore().value());
        entity.setStressScore((short) entry.getStressScore().value());
        entity.setSleepQualityScore((short) entry.getSleepQualityScore().value());
        entity.setNote(entry.getNote());
        entity.setCompleted(entry.isCompleted());
        if (entry.getCreatedAt() != null) {
            entity.setCreatedAt(entry.getCreatedAt());
        }
        if (entry.getUpdatedAt() != null) {
            entity.setUpdatedAt(entry.getUpdatedAt());
        }

        Set<TagJpaEntity> tags = new HashSet<>();
        for (UUID tagId : entry.getTagIds()) {
            tags.add(entityManager.getReference(TagJpaEntity.class, tagId));
        }
        entity.setTags(tags);

        Set<SymptomJpaEntity> symptoms = new HashSet<>();
        for (UUID symptomId : entry.getSymptomIds()) {
            symptoms.add(entityManager.getReference(SymptomJpaEntity.class, symptomId));
        }
        entity.setSymptoms(symptoms);

        return entity;
    }
}

