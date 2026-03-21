package com.mooddiary.diary.infrastructure.persistence.jpa.repository;

import com.mooddiary.diary.infrastructure.persistence.jpa.DiaryEntryJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaryEntryJpaRepository extends JpaRepository<DiaryEntryJpaEntity, UUID> {
    boolean existsByUserIdAndEntryDate(UUID userId, LocalDate entryDate);

    Optional<DiaryEntryJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    List<DiaryEntryJpaEntity> findByUserIdOrderByEntryDateDesc(UUID userId, Pageable pageable);

    List<DiaryEntryJpaEntity> findByUserIdAndEntryDateGreaterThanEqualOrderByEntryDateDesc(
            UUID userId,
            LocalDate from,
            Pageable pageable
    );

    List<DiaryEntryJpaEntity> findByUserIdAndEntryDateLessThanEqualOrderByEntryDateDesc(
            UUID userId,
            LocalDate to,
            Pageable pageable
    );

    List<DiaryEntryJpaEntity> findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
            UUID userId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    long deleteByIdAndUserId(UUID id, UUID userId);
}

