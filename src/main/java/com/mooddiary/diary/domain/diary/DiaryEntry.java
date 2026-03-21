package com.mooddiary.diary.domain.diary;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class DiaryEntry {
    private static final int NOTE_MAX_LENGTH = 10000;

    private final UUID id;
    private final UUID userId;
    private final LocalDate entryDate;
    private final Score1to10 moodScore;
    private final Score1to10 energyScore;
    private final Score1to10 productivityScore;
    private final Score1to10 stressScore;
    private final Score1to10 sleepQualityScore;
    private final String note;
    private final boolean isCompleted;
    private final Set<UUID> tagIds;
    private final Set<UUID> symptomIds;
    private final Instant createdAt;
    private final Instant updatedAt;

    private DiaryEntry(
            UUID id,
            UUID userId,
            LocalDate entryDate,
            Score1to10 moodScore,
            Score1to10 energyScore,
            Score1to10 productivityScore,
            Score1to10 stressScore,
            Score1to10 sleepQualityScore,
            String note,
            boolean isCompleted,
            Set<UUID> tagIds,
            Set<UUID> symptomIds,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.entryDate = Objects.requireNonNull(entryDate, "entryDate");
        this.moodScore = Objects.requireNonNull(moodScore, "moodScore");
        this.energyScore = Objects.requireNonNull(energyScore, "energyScore");
        this.productivityScore = Objects.requireNonNull(productivityScore, "productivityScore");
        this.stressScore = Objects.requireNonNull(stressScore, "stressScore");
        this.sleepQualityScore = Objects.requireNonNull(sleepQualityScore, "sleepQualityScore");
        if (note != null && note.length() > NOTE_MAX_LENGTH) {
            throw new IllegalArgumentException("Note is too long");
        }
        this.note = note;
        this.isCompleted = isCompleted;
        this.tagIds = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNullElse(tagIds, Collections.emptySet())));
        this.symptomIds = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNullElse(symptomIds, Collections.emptySet())));
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DiaryEntry createNew(
            UUID userId,
            LocalDate entryDate,
            Score1to10 moodScore,
            Score1to10 energyScore,
            Score1to10 productivityScore,
            Score1to10 stressScore,
            Score1to10 sleepQualityScore,
            String note,
            boolean isCompleted,
            Collection<UUID> tagIds,
            Collection<UUID> symptomIds
    ) {
        return new DiaryEntry(
                UUID.randomUUID(),
                userId,
                entryDate,
                moodScore,
                energyScore,
                productivityScore,
                stressScore,
                sleepQualityScore,
                note,
                isCompleted,
                tagIds == null ? Collections.emptySet() : new HashSet<>(tagIds),
                symptomIds == null ? Collections.emptySet() : new HashSet<>(symptomIds),
                null,
                null
        );
    }

    public static DiaryEntry fromPersistence(
            UUID id,
            UUID userId,
            LocalDate entryDate,
            Score1to10 moodScore,
            Score1to10 energyScore,
            Score1to10 productivityScore,
            Score1to10 stressScore,
            Score1to10 sleepQualityScore,
            String note,
            boolean isCompleted,
            Collection<UUID> tagIds,
            Collection<UUID> symptomIds,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new DiaryEntry(
                id,
                userId,
                entryDate,
                moodScore,
                energyScore,
                productivityScore,
                stressScore,
                sleepQualityScore,
                note,
                isCompleted,
                tagIds == null ? Collections.emptySet() : new HashSet<>(tagIds),
                symptomIds == null ? Collections.emptySet() : new HashSet<>(symptomIds),
                createdAt,
                updatedAt
        );
    }

    public DiaryEntry update(
            LocalDate entryDate,
            Score1to10 moodScore,
            Score1to10 energyScore,
            Score1to10 productivityScore,
            Score1to10 stressScore,
            Score1to10 sleepQualityScore,
            String note,
            boolean isCompleted,
            Collection<UUID> tagIds,
            Collection<UUID> symptomIds
    ) {
        return new DiaryEntry(
                this.id,
                this.userId,
                entryDate,
                moodScore,
                energyScore,
                productivityScore,
                stressScore,
                sleepQualityScore,
                note,
                isCompleted,
                tagIds == null ? Collections.emptySet() : new HashSet<>(tagIds),
                symptomIds == null ? Collections.emptySet() : new HashSet<>(symptomIds),
                this.createdAt,
                null
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public Score1to10 getMoodScore() {
        return moodScore;
    }

    public Score1to10 getEnergyScore() {
        return energyScore;
    }

    public Score1to10 getProductivityScore() {
        return productivityScore;
    }

    public Score1to10 getStressScore() {
        return stressScore;
    }

    public Score1to10 getSleepQualityScore() {
        return sleepQualityScore;
    }

    public String getNote() {
        return note;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Set<UUID> getTagIds() {
        return tagIds;
    }

    public Set<UUID> getSymptomIds() {
        return symptomIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

