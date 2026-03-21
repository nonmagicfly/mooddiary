package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.domain.symptom.Symptom;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.SymptomJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SymptomRepositoryJpaAdapter implements SymptomRepositoryPort {
    private final SymptomJpaRepository symptomJpaRepository;

    public SymptomRepositoryJpaAdapter(SymptomJpaRepository symptomJpaRepository) {
        this.symptomJpaRepository = symptomJpaRepository;
    }

    @Override
    public boolean existsByIdAndUserId(UUID symptomId, UUID userId) {
        return symptomJpaRepository.existsByIdAndUserId(symptomId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndName(UUID userId, String name) {
        return symptomJpaRepository.existsByUserIdAndName(userId, name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Symptom> findAllByUserId(UUID userId) {
        return symptomJpaRepository.findByUserIdOrderByNameAsc(userId).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Symptom> findByIdAndUserId(UUID symptomId, UUID userId) {
        return symptomJpaRepository.findByIdAndUserId(symptomId, userId).map(this::mapToDomain);
    }

    @Override
    @Transactional
    public Symptom save(Symptom symptom) {
        var entity = mapToEntity(symptom);
        var saved = symptomJpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    @Transactional
    public boolean deleteByIdAndUserId(UUID symptomId, UUID userId) {
        return symptomJpaRepository.deleteByIdAndUserId(symptomId, userId) > 0;
    }

    private Symptom mapToDomain(com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity entity) {
        return new Symptom(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity mapToEntity(Symptom symptom) {
        com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity entity = new com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity();
        entity.setId(symptom.getId());
        entity.setUserId(symptom.getUserId());
        entity.setName(symptom.getName());
        if (symptom.getCreatedAt() != null) {
            entity.setCreatedAt(symptom.getCreatedAt());
        }
        if (symptom.getUpdatedAt() != null) {
            entity.setUpdatedAt(symptom.getUpdatedAt());
        }
        return entity;
    }
}

