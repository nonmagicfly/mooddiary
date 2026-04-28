package com.mooddiary.diary.application.tag.usecase.impl;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.application.tag.TagUpdateCommand;
import com.mooddiary.diary.domain.tag.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTagUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private UpdateTagUseCaseImpl useCase;

    @Test
    void shouldUpdateHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Instant createdAt = Instant.now().minusSeconds(3600);

        Tag existing = new Tag(tagId, userId, "work", "blue", createdAt, Instant.now().minusSeconds(60));
        Tag saved = new Tag(tagId, userId, "work2", "green", createdAt, Instant.now());

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.findByIdAndUserId(tagId, userId)).thenReturn(Optional.of(existing));
        when(tagRepositoryPort.existsByUserIdAndName(userId, "work2")).thenReturn(false);
        when(tagRepositoryPort.save(org.mockito.ArgumentMatchers.any(Tag.class))).thenReturn(saved);

        TagResponse response = useCase.execute(keycloakSubject, tagId, new TagUpdateCommand("work2", "green"));

        assertEquals(tagId, response.id());
        assertEquals("work2", response.name());
        assertEquals("green", response.color());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void shouldThrowNotFoundWhenTagMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.findByIdAndUserId(tagId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, tagId, new TagUpdateCommand("x", null)));
    }

    @Test
    void shouldThrowConflictWhenNameExists() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Instant createdAt = Instant.now().minusSeconds(3600);
        Tag existing = new Tag(tagId, userId, "work", null, createdAt, null);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.findByIdAndUserId(tagId, userId)).thenReturn(Optional.of(existing));
        when(tagRepositoryPort.existsByUserIdAndName(userId, "life")).thenReturn(true);

        assertThrows(ConflictAppException.class, () -> useCase.execute(keycloakSubject, tagId, new TagUpdateCommand("life", null)));
    }

    @Test
    void shouldThrowValidationWhenTagIdNull() {
        String keycloakSubject = "sub-1";
        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, null, new TagUpdateCommand("x", null)));
    }
}

