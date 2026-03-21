package com.mooddiary.diary.domain.diary;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiaryEntryTest {
    @Test
    void shouldCreateNewWithEmptyTagsAndSymptoms() {
        UUID userId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();

        DiaryEntry entry = DiaryEntry.createNew(
                userId,
                entryDate,
                Score1to10.of(5),
                Score1to10.of(5),
                Score1to10.of(5),
                Score1to10.of(5),
                Score1to10.of(5),
                null,
                false,
                null,
                null
        );

        assertNotNull(entry.getId());
        assertEquals(userId, entry.getUserId());
        assertEquals(entryDate, entry.getEntryDate());
        assertEquals(Set.of(), entry.getTagIds());
        assertEquals(Set.of(), entry.getSymptomIds());
    }

    @Test
    void shouldRejectTooLongNote() {
        UUID userId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();

        String note = "x".repeat(10001);

        assertThrows(IllegalArgumentException.class, () -> DiaryEntry.createNew(
                userId,
                entryDate,
                Score1to10.of(5),
                Score1to10.of(5),
                Score1to10.of(5),
                Score1to10.of(5),
                Score1to10.of(5),
                note,
                false,
                List.of(),
                List.of()
        ));
    }

    @Test
    void shouldRehydrateWithTimestampsAndIds() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        Set<UUID> tagIds = Set.of(UUID.randomUUID());
        Set<UUID> symptomIds = Set.of(UUID.randomUUID(), UUID.randomUUID());

        DiaryEntry entry = DiaryEntry.fromPersistence(
                id,
                userId,
                entryDate,
                Score1to10.of(1),
                Score1to10.of(2),
                Score1to10.of(3),
                Score1to10.of(4),
                Score1to10.of(5),
                "note",
                true,
                tagIds,
                symptomIds,
                createdAt,
                updatedAt
        );

        assertEquals(id, entry.getId());
        assertEquals(createdAt, entry.getCreatedAt());
        assertEquals(updatedAt, entry.getUpdatedAt());
        assertEquals(tagIds, entry.getTagIds());
        assertEquals(symptomIds, entry.getSymptomIds());
    }
}

