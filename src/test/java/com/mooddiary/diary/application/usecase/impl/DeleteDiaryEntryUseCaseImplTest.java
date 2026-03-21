package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryDeleteResult;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteDiaryEntryUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    @InjectMocks
    private DeleteDiaryEntryUseCaseImpl useCase;

    @Test
    void shouldDeleteHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.deleteByIdAndUserId(diaryEntryId, userId)).thenReturn(true);

        DiaryEntryDeleteResult result = useCase.execute(keycloakSubject, diaryEntryId);

        assertEquals(userId, result.userId());
        assertEquals(diaryEntryId, result.diaryEntryId());
    }

    @Test
    void shouldThrowNotFoundWhenDiaryEntryMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.deleteByIdAndUserId(diaryEntryId, userId)).thenReturn(false);

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, diaryEntryId));
    }
}

