package com.mooddiary.diary.application.photo.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.photo.DeletePhotoUseCase;
import com.mooddiary.diary.application.port.out.FileStoragePort;
import com.mooddiary.diary.application.port.out.PhotoRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.domain.photo.Photo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePhotoUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private PhotoRepositoryPort photoRepositoryPort;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private DeletePhotoUseCaseImpl useCase;

    @Test
    void shouldDeletePhotoHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();

        Photo photo = new Photo(
                photoId,
                UUID.randomUUID(),
                "a.png",
                "photos/u/p/a.png",
                "image/png",
                3L,
                Instant.now()
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(photoRepositoryPort.findByIdAndUserId(photoId, userId)).thenReturn(Optional.of(photo));
        when(photoRepositoryPort.deleteByIdAndUserId(photoId, userId)).thenReturn(true);
        when(fileStoragePort.delete(photo.getFilePath())).thenReturn(true);

        UUID deleted = useCase.execute(keycloakSubject, photoId);

        assertEquals(photoId, deleted);
        verify(fileStoragePort).delete(photo.getFilePath());
    }

    @Test
    void shouldThrowNotFoundWhenPhotoMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(photoRepositoryPort.findByIdAndUserId(photoId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, photoId));
        verify(fileStoragePort, never()).delete(any(String.class));
    }

    @Test
    void shouldThrowValidationWhenPhotoIdNull() {
        assertThrows(ValidationAppException.class, () -> useCase.execute("sub-1", null));
    }
}

