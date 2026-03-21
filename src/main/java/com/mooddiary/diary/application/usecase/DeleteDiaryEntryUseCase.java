package com.mooddiary.diary.application.usecase;

import com.mooddiary.diary.application.diary.DiaryEntryDeleteResult;

import java.util.UUID;

public interface DeleteDiaryEntryUseCase {
    DiaryEntryDeleteResult execute(String keycloakSubject, UUID diaryEntryId);
}

