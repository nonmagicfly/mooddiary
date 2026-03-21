package com.mooddiary.diary.application.tag.usecase;

import com.mooddiary.diary.application.tag.TagResponse;

import java.util.List;

public interface ListTagsUseCase {
    List<TagResponse> execute(String keycloakSubject);
}

