package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryListQuery;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.exception.ValidationAppException;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDiaryEntriesUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    @InjectMocks
    private ListDiaryEntriesUseCaseImpl useCase;

    @Test
    void shouldThrowValidationWhenLimitIsTooSmall() {
        String keycloakSubject = "sub-1";
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        DiaryEntryListQuery query = new DiaryEntryListQuery(from, to, 0);

        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, query));
    }

    @Test
    void shouldReturnEntriesHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        int limit = 10;

        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        DiaryEntry e1 = DiaryEntry.fromPersistence(
                UUID.randomUUID(),
                userId,
                to,
                Score1to10.of(1),
                Score1to10.of(2),
                Score1to10.of(3),
                Score1to10.of(4),
                Score1to10.of(5),
                "n1",
                false,
                Set.of(),
                Set.of(),
                createdAt,
                updatedAt
        );

        DiaryEntry e2 = DiaryEntry.fromPersistence(
                UUID.randomUUID(),
                userId,
                to.minusDays(1),
                Score1to10.of(2),
                Score1to10.of(3),
                Score1to10.of(4),
                Score1to10.of(5),
                Score1to10.of(6),
                "n2",
                true,
                Set.of(),
                Set.of(),
                createdAt,
                updatedAt
        );

        DiaryEntryListQuery query = new DiaryEntryListQuery(from, to, limit);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByUserId(userId, from, to, limit)).thenReturn(List.of(e1, e2));

        List<DiaryEntryResponse> responses = useCase.execute(keycloakSubject, query);

        assertEquals(2, responses.size());
        assertEquals(e1.getId(), responses.get(0).id());
        assertEquals(e2.getId(), responses.get(1).id());
    }
}

