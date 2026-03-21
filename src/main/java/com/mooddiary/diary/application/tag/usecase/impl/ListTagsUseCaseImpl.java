package com.mooddiary.diary.application.tag.usecase.impl;

import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.application.tag.usecase.ListTagsUseCase;
import com.mooddiary.diary.domain.tag.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListTagsUseCaseImpl implements ListTagsUseCase {
    private final UserIdentityService userIdentityService;
    private final TagRepositoryPort tagRepositoryPort;

    public ListTagsUseCaseImpl(UserIdentityService userIdentityService, TagRepositoryPort tagRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> execute(String keycloakSubject) {
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        List<Tag> tags = tagRepositoryPort.findAllByUserId(userId);
        return tags.stream().map(this::map).toList();
    }

    private TagResponse map(Tag tag) {
        return new TagResponse(
                tag.getUserId(),
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }
}

