package com.mooddiary.diary.application.photo;

import java.util.List;
import java.util.UUID;

public interface UploadDiaryEntryPhotosUseCase {
    List<PhotoResponse> execute(String keycloakSubject, UUID diaryEntryId, UploadDiaryEntryPhotosCommand command);
}

