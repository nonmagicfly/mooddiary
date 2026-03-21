package com.mooddiary.diary.application.symptom.usecase.impl;

import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.SymptomResponse;
import com.mooddiary.diary.application.symptom.usecase.ListSymptomsUseCase;
import com.mooddiary.diary.domain.symptom.Symptom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListSymptomsUseCaseImpl implements ListSymptomsUseCase {
    private final UserIdentityService userIdentityService;
    private final SymptomRepositoryPort symptomRepositoryPort;

    public ListSymptomsUseCaseImpl(UserIdentityService userIdentityService, SymptomRepositoryPort symptomRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.symptomRepositoryPort = symptomRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SymptomResponse> execute(String keycloakSubject) {
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        List<Symptom> symptoms = symptomRepositoryPort.findAllByUserId(userId);
        return symptoms.stream().map(this::map).toList();
    }

    private SymptomResponse map(Symptom symptom) {
        return new SymptomResponse(
                symptom.getUserId(),
                symptom.getId(),
                symptom.getName(),
                symptom.getCreatedAt(),
                symptom.getUpdatedAt()
        );
    }
}

