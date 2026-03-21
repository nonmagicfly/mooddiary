package com.mooddiary.diary.application.usecase;

import com.mooddiary.diary.application.diary.DiaryEntryCreateCommand;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;

public interface CreateDiaryEntryUseCase {
    DiaryEntryResponse execute(String keycloakSubject, DiaryEntryCreateCommand command);
}

