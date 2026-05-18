package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDiaryEntryUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    @InjectMocks
    private GetDiaryEntryUseCaseImpl useCase;

    @Test
    void shouldGetDiaryEntryHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now().minusDays(10);

        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        DiaryEntry entry = DiaryEntry.fromPersistence(
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
                Set.of(),
                Set.of(),
                createdAt,
                updatedAt
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.of(entry));

        DiaryEntryResponse response = useCase.execute(keycloakSubject, diaryEntryId);

        assertEquals(diaryEntryId, response.id());
        assertEquals(userId, response.userId());
        assertEquals(entryDate, response.entryDate());
        assertEquals(1, response.moodScore());
        assertEquals("note", response.note());
        assertEquals(true, response.isCompleted());
    }

    @Test
    void shouldThrowNotFoundWhenEntryMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, diaryEntryId));
    }
}

