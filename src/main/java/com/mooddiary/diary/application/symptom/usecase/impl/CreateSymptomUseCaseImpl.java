package com.mooddiary.diary.application.symptom.usecase.impl;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.SymptomCreateCommand;
import com.mooddiary.diary.application.symptom.SymptomResponse;
import com.mooddiary.diary.application.symptom.usecase.CreateSymptomUseCase;
import com.mooddiary.diary.domain.symptom.Symptom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateSymptomUseCaseImpl implements CreateSymptomUseCase {
    private static final int NAME_MAX_LENGTH = 255;

    private final UserIdentityService userIdentityService;
    private final SymptomRepositoryPort symptomRepositoryPort;

    public CreateSymptomUseCaseImpl(UserIdentityService userIdentityService, SymptomRepositoryPort symptomRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.symptomRepositoryPort = symptomRepositoryPort;
    }

    @Override
    @Transactional
    public SymptomResponse execute(String keycloakSubject, SymptomCreateCommand command) {
        if (command == null) {
            throw new ValidationAppException("command is required");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);

        String name = command.name();
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationAppException("name is required");
        }
        name = name.trim();
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ValidationAppException("name is too long");
        }

        if (symptomRepositoryPort.existsByUserIdAndName(userId, name)) {
            throw new ConflictAppException("Symptom with this name already exists");
        }

        Symptom symptom = new Symptom(UUID.randomUUID(), userId, name, null, null);
        Symptom saved = symptomRepositoryPort.save(symptom);
        return map(saved);
    }

    private SymptomResponse map(Symptom saved) {
        return new SymptomResponse(
                saved.getUserId(),
                saved.getId(),
                saved.getName(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}

