package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryDeleteResult;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.usecase.DeleteDiaryEntryUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteDiaryEntryUseCaseImpl implements DeleteDiaryEntryUseCase {
    private final UserIdentityService userIdentityService;
    private final DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    public DeleteDiaryEntryUseCaseImpl(UserIdentityService userIdentityService, DiaryEntryRepositoryPort diaryEntryRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.diaryEntryRepositoryPort = diaryEntryRepositoryPort;
    }

    @Override
    @Transactional
    public DiaryEntryDeleteResult execute(String keycloakSubject, UUID diaryEntryId) {
        if (diaryEntryId == null) {
            throw new NotFoundAppException("Diary entry not found");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        boolean deleted = diaryEntryRepositoryPort.deleteByIdAndUserId(diaryEntryId, userId);
        if (!deleted) {
            throw new NotFoundAppException("Diary entry not found");
        }
        return new DiaryEntryDeleteResult(userId, diaryEntryId);
    }
}

