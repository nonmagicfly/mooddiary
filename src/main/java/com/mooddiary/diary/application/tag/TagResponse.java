package com.mooddiary.diary.application.tag;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(
        UUID userId,
        UUID id,
        String name,
        String color,
        Instant createdAt,
        Instant updatedAt
) {
}

