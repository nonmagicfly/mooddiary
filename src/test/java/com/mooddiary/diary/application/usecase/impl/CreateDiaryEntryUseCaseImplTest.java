package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryCreateCommand;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import com.mooddiary.diary.domain.diary.Score1to10;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDiaryEntryUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @Mock
    private SymptomRepositoryPort symptomRepositoryPort;

    @InjectMocks
    private CreateDiaryEntryUseCaseImpl useCase;

    @Test
    void shouldCreateDiaryEntryHappyPath() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";
        LocalDate entryDate = LocalDate.now();
        UUID tagId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.existsByUserIdAndEntryDate(userId, entryDate)).thenReturn(false);
        when(tagRepositoryPort.existsByIdAndUserId(tagId, userId)).thenReturn(true);
        when(symptomRepositoryPort.existsByIdAndUserId(symptomId, userId)).thenReturn(true);

        DiaryEntry saved = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                entryDate,
                Score1to10.of(1),
                Score1to10.of(2),
                Score1to10.of(3),
                Score1to10.of(4),
                Score1to10.of(5),
                "note",
                true,
                Set.of(tagId),
                Set.of(symptomId),
                createdAt,
                updatedAt
        );
        when(diaryEntryRepositoryPort.save(org.mockito.ArgumentMatchers.any(DiaryEntry.class))).thenReturn(saved);

        DiaryEntryCreateCommand command = new DiaryEntryCreateCommand(
                entryDate,
                1,
                2,
                3,
                4,
                5,
                "note",
                true,
                Set.of(tagId),
                Set.of(symptomId)
        );

        DiaryEntryResponse response = useCase.execute(keycloakSubject, command);

        assertEquals(diaryEntryId, response.id());
        assertEquals(userId, response.userId());
        assertEquals(entryDate, response.entryDate());
        assertEquals(1, response.moodScore());
        assertEquals(2, response.energyScore());
        assertEquals(3, response.productivityScore());
        assertEquals(4, response.stressScore());
        assertEquals(5, response.sleepQualityScore());
        assertEquals("note", response.note());
        assertEquals(true, response.isCompleted());
        assertEquals(Set.of(tagId), response.tagIds());
        assertEquals(Set.of(symptomId), response.symptomIds());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());

        ArgumentCaptor<DiaryEntry> captor = ArgumentCaptor.forClass(DiaryEntry.class);
        verify(diaryEntryRepositoryPort).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(entryDate, captor.getValue().getEntryDate());
        assertEquals(Set.of(tagId), captor.getValue().getTagIds());
        assertEquals(Set.of(symptomId), captor.getValue().getSymptomIds());
    }

    @Test
    void shouldThrowConflictWhenEntryExists() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";
        LocalDate entryDate = LocalDate.now();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.existsByUserIdAndEntryDate(userId, entryDate)).thenReturn(true);

        DiaryEntryCreateCommand command = new DiaryEntryCreateCommand(
                entryDate, 1, 2, 3, 4, 5,
                null,
                false,
                Set.of(),
                Set.of()
        );

        assertThrows(ConflictAppException.class, () -> useCase.execute(keycloakSubject, command));
    }

    @Test
    void shouldThrowValidationWhenTagOwnershipInvalid() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";
        LocalDate entryDate = LocalDate.now();
        UUID tagId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.existsByUserIdAndEntryDate(userId, entryDate)).thenReturn(false);
        when(tagRepositoryPort.existsByIdAndUserId(tagId, userId)).thenReturn(false);

        DiaryEntryCreateCommand command = new DiaryEntryCreateCommand(
                entryDate, 1, 2, 3, 4, 5,
                null,
                false,
                Set.of(tagId),
                Set.of()
        );

        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, command));
    }

    @Test
    void shouldThrowValidationWhenSymptomOwnershipInvalid() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";
        LocalDate entryDate = LocalDate.now();
        UUID symptomId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.existsByUserIdAndEntryDate(userId, entryDate)).thenReturn(false);
        when(symptomRepositoryPort.existsByIdAndUserId(symptomId, userId)).thenReturn(false);

        DiaryEntryCreateCommand command = new DiaryEntryCreateCommand(
                entryDate, 1, 2, 3, 4, 5,
                null,
                false,
                Set.of(),
                Set.of(symptomId)
        );

        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, command));
    }
}

