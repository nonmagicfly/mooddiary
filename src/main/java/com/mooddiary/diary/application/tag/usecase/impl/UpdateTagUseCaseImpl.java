package com.mooddiary.diary.application.tag.usecase.impl;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.application.tag.TagUpdateCommand;
import com.mooddiary.diary.application.tag.usecase.UpdateTagUseCase;
import com.mooddiary.diary.domain.tag.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateTagUseCaseImpl implements UpdateTagUseCase {
    private static final int NAME_MAX_LENGTH = 255;

    private final UserIdentityService userIdentityService;
    private final TagRepositoryPort tagRepositoryPort;

    public UpdateTagUseCaseImpl(UserIdentityService userIdentityService, TagRepositoryPort tagRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.tagRepositoryPort = tagRepositoryPort;
    }

    @Override
    @Transactional
    public TagResponse execute(String keycloakSubject, UUID tagId, TagUpdateCommand command) {
        if (tagId == null) {
            throw new ValidationAppException("tagId is required");
        }
        if (command == null) {
            throw new ValidationAppException("command is required");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);

        Tag existing = tagRepositoryPort.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new NotFoundAppException("Tag not found"));

        String name = command.name();
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationAppException("name is required");
        }
        name = name.trim();
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ValidationAppException("name is too long");
        }
        String color = command.color();
        if (color != null && color.length() > 64) {
            throw new ValidationAppException("color is too long");
        }

        boolean nameChanged = !existing.getName().equals(name);
        if (nameChanged && tagRepositoryPort.existsByUserIdAndName(userId, name)) {
            throw new ConflictAppException("Tag with this name already exists");
        }

        Tag updated = new Tag(existing.getId(), userId, name, color, existing.getCreatedAt(), null);
        Tag saved = tagRepositoryPort.save(updated);
        return map(saved);
    }

    private TagResponse map(Tag saved) {
        return new TagResponse(
                saved.getUserId(),
                saved.getId(),
                saved.getName(),
                saved.getColor(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}

