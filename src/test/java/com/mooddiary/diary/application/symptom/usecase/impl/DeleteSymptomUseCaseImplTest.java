package com.mooddiary.diary.application.symptom.usecase.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.usecase.DeleteSymptomUseCase;
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
class DeleteSymptomUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private SymptomRepositoryPort symptomRepositoryPort;

    @InjectMocks
    private DeleteSymptomUseCaseImpl useCase;

    @Test
    void shouldDeleteHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.deleteByIdAndUserId(symptomId, userId)).thenReturn(true);

        UUID deleted = useCase.execute(keycloakSubject, symptomId);
        assertEquals(symptomId, deleted);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteFailed() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.deleteByIdAndUserId(symptomId, userId)).thenReturn(false);

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, symptomId));
    }

    @Test
    void shouldThrowValidationWhenSymptomIdNull() {
        String keycloakSubject = "sub-1";
        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, null));
    }
}

