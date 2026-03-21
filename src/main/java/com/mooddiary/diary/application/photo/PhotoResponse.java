package com.mooddiary.diary.application.photo;

import java.time.Instant;
import java.util.UUID;

public record PhotoResponse(
        UUID id,
        UUID entryId,
        String fileName,
        String filePath,
        String contentType,
        long size,
        Instant createdAt
) {
}

