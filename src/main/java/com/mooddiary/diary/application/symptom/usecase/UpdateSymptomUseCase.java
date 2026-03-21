package com.mooddiary.diary.application.symptom.usecase;

import com.mooddiary.diary.application.symptom.SymptomResponse;
import com.mooddiary.diary.application.symptom.SymptomUpdateCommand;

import java.util.UUID;

public interface UpdateSymptomUseCase {
    SymptomResponse execute(String keycloakSubject, UUID symptomId, SymptomUpdateCommand command);
}

