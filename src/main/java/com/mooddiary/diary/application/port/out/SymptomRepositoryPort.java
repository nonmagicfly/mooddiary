package com.mooddiary.diary.application.port.out;

import com.mooddiary.diary.domain.symptom.Symptom;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SymptomRepositoryPort {

    boolean existsByIdAndUserId(UUID symptomId, UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    List<Symptom> findAllByUserId(UUID userId);

    Optional<Symptom> findByIdAndUserId(UUID symptomId, UUID userId);

    Symptom save(Symptom symptom);

    boolean deleteByIdAndUserId(UUID symptomId, UUID userId);
}
