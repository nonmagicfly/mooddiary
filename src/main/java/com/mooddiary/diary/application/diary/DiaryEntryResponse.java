package com.mooddiary.diary.application.diary;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record DiaryEntryResponse(
        UUID userId,
        UUID id,
        LocalDate entryDate,
        int moodScore,
        int energyScore,
        int productivityScore,
        int stressScore,
        int sleepQualityScore,
        String note,
        boolean isCompleted,
        Set<UUID> tagIds,
        Set<UUID> symptomIds,
        Instant createdAt,
        Instant updatedAt
) {
}

