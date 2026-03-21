package com.mooddiary.diary.application.usecase;

import com.mooddiary.diary.application.diary.DiaryEntryListQuery;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;

import java.util.List;

public interface ListDiaryEntriesUseCase {
    List<DiaryEntryResponse> execute(String keycloakSubject, DiaryEntryListQuery query);
}

