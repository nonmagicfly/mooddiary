package com.mooddiary.diary.application.usecase;

import com.mooddiary.diary.application.diary.DiaryEntryResponse;

import java.util.UUID;

public interface GetDiaryEntryUseCase {
    DiaryEntryResponse execute(String keycloakSubject, UUID diaryEntryId);
}

