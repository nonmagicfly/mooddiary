package com.mooddiary.diary.application.tag.usecase;

import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.application.tag.TagUpdateCommand;

import java.util.UUID;

public interface UpdateTagUseCase {
    TagResponse execute(String keycloakSubject, UUID tagId, TagUpdateCommand command);
}

