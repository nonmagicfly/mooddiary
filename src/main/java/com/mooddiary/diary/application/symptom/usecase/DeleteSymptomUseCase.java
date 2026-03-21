package com.mooddiary.diary.application.symptom.usecase;

import java.util.UUID;

public interface DeleteSymptomUseCase {
    UUID execute(String keycloakSubject, UUID symptomId);
}

