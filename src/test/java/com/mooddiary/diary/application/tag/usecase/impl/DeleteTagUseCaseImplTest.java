package com.mooddiary.diary.application.tag.usecase.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.usecase.DeleteTagUseCase;
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
class DeleteTagUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private DeleteTagUseCaseImpl useCase;

    @Test
    void shouldDeleteHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.deleteByIdAndUserId(tagId, userId)).thenReturn(true);

        UUID deleted = useCase.execute(keycloakSubject, tagId);
        assertEquals(tagId, deleted);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteFailed() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.deleteByIdAndUserId(tagId, userId)).thenReturn(false);

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, tagId));
    }

    @Test
    void shouldThrowValidationWhenTagIdNull() {
        String keycloakSubject = "sub-1";
        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, null));
    }
}

