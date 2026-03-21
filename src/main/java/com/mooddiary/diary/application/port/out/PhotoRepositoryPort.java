package com.mooddiary.diary.application.port.out;

import com.mooddiary.diary.domain.photo.Photo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepositoryPort {

    Photo save(Photo photo);

    Optional<Photo> findByIdAndUserId(UUID photoId, UUID userId);

    List<Photo> findByDiaryEntryIdAndUserId(UUID diaryEntryId, UUID userId);

    boolean deleteByIdAndUserId(UUID photoId, UUID userId);
}
