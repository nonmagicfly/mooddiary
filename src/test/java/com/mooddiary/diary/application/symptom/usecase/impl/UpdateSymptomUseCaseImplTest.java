package com.mooddiary.diary.application.symptom.usecase.impl;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.SymptomResponse;
import com.mooddiary.diary.application.symptom.SymptomUpdateCommand;
import com.mooddiary.diary.domain.symptom.Symptom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSymptomUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private SymptomRepositoryPort symptomRepositoryPort;

    @InjectMocks
    private UpdateSymptomUseCaseImpl useCase;

    @Test
    void shouldUpdateHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);

        Symptom existing = new Symptom(symptomId, userId, "pain", createdAt, Instant.now().minusSeconds(60));
        Symptom saved = new Symptom(symptomId, userId, "pain2", createdAt, Instant.now());

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.findByIdAndUserId(symptomId, userId)).thenReturn(Optional.of(existing));
        when(symptomRepositoryPort.existsByUserIdAndName(userId, "pain2")).thenReturn(false);
        when(symptomRepositoryPort.save(org.mockito.ArgumentMatchers.any(Symptom.class))).thenReturn(saved);

        SymptomResponse response = useCase.execute(keycloakSubject, symptomId, new SymptomUpdateCommand("pain2"));

        assertEquals(symptomId, response.id());
        assertEquals("pain2", response.name());
    }

    @Test
    void shouldThrowNotFoundWhenSymptomMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.findByIdAndUserId(symptomId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, symptomId, new SymptomUpdateCommand("x")));
    }

    @Test
    void shouldThrowConflictWhenNameExists() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);

        Symptom existing = new Symptom(symptomId, userId, "pain", createdAt, null);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(symptomRepositoryPort.findByIdAndUserId(symptomId, userId)).thenReturn(Optional.of(existing));
        when(symptomRepositoryPort.existsByUserIdAndName(userId, "pain2")).thenReturn(true);

        assertThrows(ConflictAppException.class, () -> useCase.execute(keycloakSubject, symptomId, new SymptomUpdateCommand("pain2")));
    }

    @Test
    void shouldThrowValidationWhenSymptomIdNull() {
        String keycloakSubject = "sub-1";
        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, null, new SymptomUpdateCommand("x")));
    }
}

