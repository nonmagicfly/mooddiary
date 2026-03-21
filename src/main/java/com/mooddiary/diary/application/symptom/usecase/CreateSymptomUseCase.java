package com.mooddiary.diary.application.symptom.usecase;

import com.mooddiary.diary.application.symptom.SymptomCreateCommand;
import com.mooddiary.diary.application.symptom.SymptomResponse;

public interface CreateSymptomUseCase {
    SymptomResponse execute(String keycloakSubject, SymptomCreateCommand command);
}

