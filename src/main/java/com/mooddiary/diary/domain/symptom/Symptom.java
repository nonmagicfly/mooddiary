package com.mooddiary.diary.domain.symptom;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Symptom {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Symptom(UUID id, UUID userId, String name, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.name = Objects.requireNonNull(name, "name");
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

