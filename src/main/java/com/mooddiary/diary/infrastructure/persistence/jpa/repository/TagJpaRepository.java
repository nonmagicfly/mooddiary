package com.mooddiary.diary.infrastructure.persistence.jpa.repository;

import com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, UUID> {
    boolean existsByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    List<TagJpaEntity> findByUserIdOrderByNameAsc(UUID userId);

    Optional<TagJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    long deleteByIdAndUserId(UUID id, UUID userId);
}

