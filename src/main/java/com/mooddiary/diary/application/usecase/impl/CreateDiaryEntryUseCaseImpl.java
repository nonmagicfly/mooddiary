package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryCreateCommand;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.port.out.SymptomRepositoryPort;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.usecase.CreateDiaryEntryUseCase;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import com.mooddiary.diary.domain.diary.Score1to10;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Service
public class CreateDiaryEntryUseCaseImpl implements CreateDiaryEntryUseCase {
    private final UserIdentityService userIdentityService;
    private final DiaryEntryRepositoryPort diaryEntryRepositoryPort;
    private final TagRepositoryPort tagRepositoryPort;
    private final SymptomRepositoryPort symptomRepositoryPort;

    public CreateDiaryEntryUseCaseImpl(
            UserIdentityService userIdentityService,
            DiaryEntryRepositoryPort diaryEntryRepositoryPort,
            TagRepositoryPort tagRepositoryPort,
            SymptomRepositoryPort symptomRepositoryPort
    ) {
        this.userIdentityService = userIdentityService;
        this.diaryEntryRepositoryPort = diaryEntryRepositoryPort;
        this.tagRepositoryPort = tagRepositoryPort;
        this.symptomRepositoryPort = symptomRepositoryPort;
    }

    @Override
    @Transactional
    public DiaryEntryResponse execute(String keycloakSubject, DiaryEntryCreateCommand command) {
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        if (command.entryDate() == null) {
            throw new ValidationAppException("entryDate is required");
        }
        boolean exists = diaryEntryRepositoryPort.existsByUserIdAndEntryDate(userId, command.entryDate());
        if (exists) {
            throw new ConflictAppException("Diary entry for this date already exists");
        }

        validateTagIds(userId, command.tagIds());
        validateSymptomIds(userId, command.symptomIds());

        try {
            DiaryEntry entry = DiaryEntry.createNew(
                    userId,
                    command.entryDate(),
                    Score1to10.of(command.moodScore()),
                    Score1to10.of(command.energyScore()),
                    Score1to10.of(command.productivityScore()),
                    Score1to10.of(command.stressScore()),
                    Score1to10.of(command.sleepQualityScore()),
                    command.note(),
                    command.isCompleted(),
                    command.tagIds(),
                    command.symptomIds()
            );
            DiaryEntry saved = diaryEntryRepositoryPort.save(entry);
            return map(saved);
        } catch (IllegalArgumentException ex) {
            throw new ValidationAppException(ex.getMessage());
        }
    }

    private void validateTagIds(UUID userId, Collection<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (UUID tagId : tagIds) {
            if (tagId == null) {
                throw new ValidationAppException("tagId is required");
            }
            boolean exists = tagRepositoryPort.existsByIdAndUserId(tagId, userId);
            if (!exists) {
                throw new ValidationAppException("Invalid tagId");
            }
        }
    }

    private void validateSymptomIds(UUID userId, Collection<UUID> symptomIds) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return;
        }
        for (UUID symptomId : symptomIds) {
            if (symptomId == null) {
                throw new ValidationAppException("symptomId is required");
            }
            boolean exists = symptomRepositoryPort.existsByIdAndUserId(symptomId, userId);
            if (!exists) {
                throw new ValidationAppException("Invalid symptomId");
            }
        }
    }

    private DiaryEntryResponse map(DiaryEntry saved) {
        return new DiaryEntryResponse(
                saved.getUserId(),
                saved.getId(),
                saved.getEntryDate(),
                saved.getMoodScore().value(),
                saved.getEnergyScore().value(),
                saved.getProductivityScore().value(),
                saved.getStressScore().value(),
                saved.getSleepQualityScore().value(),
                saved.getNote(),
                saved.isCompleted(),
                saved.getTagIds(),
                saved.getSymptomIds(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}

