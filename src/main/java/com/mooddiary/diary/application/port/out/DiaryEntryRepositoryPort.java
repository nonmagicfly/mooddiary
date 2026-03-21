package com.mooddiary.diary.application.port.out;

import com.mooddiary.diary.domain.diary.DiaryEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaryEntryRepositoryPort {

    boolean existsByUserIdAndEntryDate(UUID userId, LocalDate entryDate);

    Optional<DiaryEntry> findByIdAndUserId(UUID diaryEntryId, UUID userId);

    List<DiaryEntry> findByUserId(UUID userId, LocalDate from, LocalDate to, int limit);

    DiaryEntry save(DiaryEntry entry);

    boolean deleteByIdAndUserId(UUID diaryEntryId, UUID userId);
}
