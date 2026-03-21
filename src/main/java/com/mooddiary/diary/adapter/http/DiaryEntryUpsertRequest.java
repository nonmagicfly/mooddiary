package com.mooddiary.diary.adapter.http;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record DiaryEntryUpsertRequest(
        @NotNull @PastOrPresent LocalDate entryDate,
        @NotNull @Min(1) @Max(10) Integer moodScore,
        @NotNull @Min(1) @Max(10) Integer energyScore,
        @NotNull @Min(1) @Max(10) Integer productivityScore,
        @NotNull @Min(1) @Max(10) Integer stressScore,
        @NotNull @Min(1) @Max(10) Integer sleepQualityScore,
        @Size(max = 10000) String note,
        boolean isCompleted,
        Set<UUID> tagIds,
        Set<UUID> symptomIds
) {
    public DiaryEntryUpsertRequest {
        tagIds = tagIds == null ? Set.of() : Set.copyOf(tagIds);
        symptomIds = symptomIds == null ? Set.of() : Set.copyOf(symptomIds);
    }
}

