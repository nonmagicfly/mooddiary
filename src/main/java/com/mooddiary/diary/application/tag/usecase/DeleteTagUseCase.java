package com.mooddiary.diary.application.tag.usecase;

import java.util.UUID;

public interface DeleteTagUseCase {
    UUID execute(String keycloakSubject, UUID tagId);
}

