package com.mooddiary.diary.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {
    private final UUID id;
    private final String keycloakSubject;
    private final Instant createdAt;
    private final Instant updatedAt;

    public User(UUID id, String keycloakSubject, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.keycloakSubject = Objects.requireNonNull(keycloakSubject, "keycloakSubject");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakSubject() {
        return keycloakSubject;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

