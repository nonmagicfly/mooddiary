package com.mooddiary.diary.application.symptom;

import java.time.Instant;
import java.util.UUID;

public record SymptomResponse(
        UUID userId,
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}

