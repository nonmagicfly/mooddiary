package com.mooddiary.diary.application.tag.usecase.impl;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.TagCreateCommand;
import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.domain.tag.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTagUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private TagRepositoryPort tagRepositoryPort;

    @InjectMocks
    private CreateTagUseCaseImpl useCase;

    @Test
    void shouldCreateTagHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();

        UUID tagId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.existsByUserIdAndName(userId, "work")).thenReturn(false);

        Tag saved = new Tag(tagId, userId, "work", "red", createdAt, updatedAt);
        when(tagRepositoryPort.save(org.mockito.ArgumentMatchers.any(Tag.class))).thenReturn(saved);

        TagResponse response = useCase.execute(keycloakSubject, new TagCreateCommand("work", "red"));

        assertEquals(userId, response.userId());
        assertEquals(tagId, response.id());
        assertEquals("work", response.name());
        assertEquals("red", response.color());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
    }

    @Test
    void shouldThrowConflictWhenNameExists() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(tagRepositoryPort.existsByUserIdAndName(userId, "work")).thenReturn(true);

        assertThrows(ConflictAppException.class, () -> useCase.execute(keycloakSubject, new TagCreateCommand("work", null)));
    }

    @Test
    void shouldThrowValidationWhenNameEmpty() {
        String keycloakSubject = "sub-1";

        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, new TagCreateCommand(" ", null)));
    }
}

