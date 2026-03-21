package com.mooddiary.diary.application.symptom.usecase;

import com.mooddiary.diary.application.symptom.SymptomResponse;

import java.util.List;

public interface ListSymptomsUseCase {
    List<SymptomResponse> execute(String keycloakSubject);
}

