package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.usecase.GetDiaryEntryUseCase;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetDiaryEntryUseCaseImpl implements GetDiaryEntryUseCase {
    private final UserIdentityService userIdentityService;
    private final DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    public GetDiaryEntryUseCaseImpl(UserIdentityService userIdentityService, DiaryEntryRepositoryPort diaryEntryRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.diaryEntryRepositoryPort = diaryEntryRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public DiaryEntryResponse execute(String keycloakSubject, UUID diaryEntryId) {
        if (diaryEntryId == null) {
            throw new NotFoundAppException("Diary entry not found");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        DiaryEntry entry = diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)
                .orElseThrow(() -> new NotFoundAppException("Diary entry not found"));
        return map(entry);
    }

    private DiaryEntryResponse map(DiaryEntry entry) {
        return new DiaryEntryResponse(
                entry.getUserId(),
                entry.getId(),
                entry.getEntryDate(),
                entry.getMoodScore().value(),
                entry.getEnergyScore().value(),
                entry.getProductivityScore().value(),
                entry.getStressScore().value(),
                entry.getSleepQualityScore().value(),
                entry.getNote(),
                entry.isCompleted(),
                entry.getTagIds(),
                entry.getSymptomIds(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}

