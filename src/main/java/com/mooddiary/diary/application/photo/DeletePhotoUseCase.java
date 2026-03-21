package com.mooddiary.diary.application.photo;

import java.util.UUID;

public interface DeletePhotoUseCase {
    UUID execute(String keycloakSubject, UUID photoId);
}

