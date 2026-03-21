package com.mooddiary.diary.application.tag.usecase;

import com.mooddiary.diary.application.tag.TagCreateCommand;
import com.mooddiary.diary.application.tag.TagResponse;

public interface CreateTagUseCase {
    TagResponse execute(String keycloakSubject, TagCreateCommand command);
}

