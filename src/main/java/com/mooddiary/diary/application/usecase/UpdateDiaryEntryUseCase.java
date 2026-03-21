package com.mooddiary.diary.application.usecase;

import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.diary.DiaryEntryUpdateCommand;

import java.util.UUID;

public interface UpdateDiaryEntryUseCase {
    DiaryEntryResponse execute(String keycloakSubject, UUID diaryEntryId, DiaryEntryUpdateCommand command);
}

