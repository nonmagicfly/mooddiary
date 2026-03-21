package com.mooddiary.diary.application.symptom.usecase.impl;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.SymptomCreateCommand;
import com.mooddiary.diary.application.symptom.SymptomResponse;
import com.mooddiary.diary.domain.symptom.Symptom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSymptomUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private SymptomRepositoryPort symptomRepositoryPort;

    @InjectMocks
    private CreateSymptomUseCaseImpl useCase;

    @Test
    void shouldCreateSymptomHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.existsByUserIdAndName(userId, "pain")).thenReturn(false);

        Symptom saved = new Symptom(symptomId, userId, "pain", createdAt, updatedAt);
        when(symptomRepositoryPort.save(org.mockito.ArgumentMatchers.any(Symptom.class))).thenReturn(saved);

        SymptomResponse response = useCase.execute(keycloakSubject, new SymptomCreateCommand("pain"));

        assertEquals(userId, response.userId());
        assertEquals(symptomId, response.id());
        assertEquals("pain", response.name());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
    }

    @Test
    void shouldThrowConflictWhenNameExists() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.existsByUserIdAndName(userId, "pain")).thenReturn(true);

        assertThrows(ConflictAppException.class, () -> useCase.execute(keycloakSubject, new SymptomCreateCommand("pain")));
    }

    @Test
    void shouldThrowValidationWhenNameEmpty() {
        String keycloakSubject = "sub-1";
        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, new SymptomCreateCommand("  ")));
    }
}

