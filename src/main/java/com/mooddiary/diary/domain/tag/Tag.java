package com.mooddiary.diary.domain.tag;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Tag {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String color;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Tag(UUID id, UUID userId, String name, String color, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.name = Objects.requireNonNull(name, "name");
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

