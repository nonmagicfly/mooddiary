package com.mooddiary.diary.application.tag.usecase.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.usecase.DeleteTagUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteTagUseCaseImpl implements DeleteTagUseCase {
    private final UserIdentityService userIdentityService;
    private final TagRepositoryPort tagRepositoryPort;

    public DeleteTagUseCaseImpl(UserIdentityService userIdentityService, TagRepositoryPort tagRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional
    public UUID execute(String keycloakSubject, UUID tagId) {
        if (tagId == null) {
            throw new ValidationAppException("tagId is required");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);

        boolean deleted = tagRepositoryPort.deleteByIdAndUserId(tagId, userId);
        if (!deleted) {
            throw new NotFoundAppException("Tag not found");
        }
        return tagId;
    }
}

