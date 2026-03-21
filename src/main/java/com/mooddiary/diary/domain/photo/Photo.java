package com.mooddiary.diary.domain.photo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Photo {
    private final UUID id;
    private final UUID entryId;
    private final String fileName;
    private final String filePath;
    private final String contentType;
    private final long size;
    private final Instant createdAt;

    public Photo(
            UUID id,
            UUID entryId,
            String fileName,
            String filePath,
            String contentType,
            long size,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.entryId = Objects.requireNonNull(entryId, "entryId");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.filePath = Objects.requireNonNull(filePath, "filePath");
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

