package com.mooddiary.diary.infrastructure.persistence.jpa.repository;

import com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface SymptomJpaRepository extends JpaRepository<SymptomJpaEntity, UUID> {
    boolean existsByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    List<SymptomJpaEntity> findByUserIdOrderByNameAsc(UUID userId);

    Optional<SymptomJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    long deleteByIdAndUserId(UUID id, UUID userId);
}

