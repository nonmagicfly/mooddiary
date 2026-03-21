package com.mooddiary.diary.application.symptom.usecase.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.usecase.DeleteSymptomUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteSymptomUseCaseImpl implements DeleteSymptomUseCase {
    private final UserIdentityService userIdentityService;
    private final SymptomRepositoryPort symptomRepositoryPort;

    public DeleteSymptomUseCaseImpl(UserIdentityService userIdentityService, SymptomRepositoryPort symptomRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.symptomRepositoryPort = symptomRepositoryPort;
    }

    @Override
    @Transactional
    public UUID execute(String keycloakSubject, UUID symptomId) {
        if (symptomId == null) {
            throw new ValidationAppException("symptomId is required");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);

        boolean deleted = symptomRepositoryPort.deleteByIdAndUserId(symptomId, userId);
        if (!deleted) {
            throw new NotFoundAppException("Symptom not found");
        }
        return symptomId;
    }
}

