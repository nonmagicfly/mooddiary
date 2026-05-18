package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.diary.DiaryEntryUpdateCommand;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import com.mooddiary.diary.domain.diary.Score1to10;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDiaryEntryUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @Mock
    private SymptomRepositoryPort symptomRepositoryPort;

    @InjectMocks
    private UpdateDiaryEntryUseCaseImpl useCase;

    @Test
    void shouldUpdateDiaryEntryHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();
        UUID tagId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        DiaryEntry existing = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                entryDate,
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                "old",
                false,
                Set.of(tagId),
                Set.of(symptomId),
                createdAt,
                updatedAt
        );

        DiaryEntry saved = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                entryDate,
                Score1to10.of(2),
                Score1to10.of(3),
                Score1to10.of(4),
                Score1to10.of(5),
                Score1to10.of(6),
                "new",
                true,
                Set.of(tagId),
                Set.of(symptomId),
                createdAt,
                Instant.now()
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.of(existing));
        when(tagRepositoryPort.existsByIdAndUserId(tagId, userId)).thenReturn(true);
        when(symptomRepositoryPort.existsByIdAndUserId(symptomId, userId)).thenReturn(true);
        when(diaryEntryRepositoryPort.save(any(DiaryEntry.class))).thenReturn(saved);

        DiaryEntryUpdateCommand command = new DiaryEntryUpdateCommand(
                entryDate,
                2,
                3,
                4,
                5,
                6,
                "new",
                true,
                Set.of(tagId),
                Set.of(symptomId)
        );

        DiaryEntryResponse response = useCase.execute(keycloakSubject, diaryEntryId, command);

        assertEquals(diaryEntryId, response.id());
        assertEquals(2, response.moodScore());
        assertEquals(3, response.energyScore());
        assertEquals(4, response.productivityScore());
        assertEquals(5, response.stressScore());
        assertEquals(6, response.sleepQualityScore());
        assertEquals("new", response.note());
        assertEquals(true, response.isCompleted());
    }

    @Test
    void shouldThrowNotFoundWhenEntryMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.empty());

        DiaryEntryUpdateCommand command = new DiaryEntryUpdateCommand(
                entryDate, 1, 2, 3, 4, 5,
                null, false,
                Set.of(), Set.of()
        );

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, diaryEntryId, command));
    }

    @Test
    void shouldThrowValidationWhenEditWindowExpired() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate oldEntryDate = LocalDate.now().minusDays(5);

        DiaryEntry existing = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                oldEntryDate,
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                "x",
                true,
                Set.of(),
                Set.of(),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60)
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.of(existing));

        DiaryEntryUpdateCommand command = new DiaryEntryUpdateCommand(
                oldEntryDate, 2, 2, 2, 2, 2,
                "y", false,
                Set.of(), Set.of()
        );

        ValidationAppException ex = assertThrows(ValidationAppException.class,
                () -> useCase.execute(keycloakSubject, diaryEntryId, command));
        assertTrue(ex.getMessage().contains("Срок редактирования"));
    }

    @Test
    void shouldThrowValidationWhenTagOwnershipInvalid() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();
        UUID tagId = UUID.randomUUID();

        DiaryEntry existing = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                entryDate,
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                null,
                false,
                Set.of(),
                Set.of(),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60)
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.of(existing));
        when(tagRepositoryPort.existsByIdAndUserId(tagId, userId)).thenReturn(false);

        DiaryEntryUpdateCommand command = new DiaryEntryUpdateCommand(
                entryDate, 1, 2, 3, 4, 5,
                null, false,
                Set.of(tagId), Set.of()
        );

        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, diaryEntryId, command));
    }
}

