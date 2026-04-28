package com.mooddiary.diary.application.photo.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.photo.PhotoResponse;
import com.mooddiary.diary.application.photo.PhotoUploadFileCommand;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosCommand;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.port.out.FileStoragePort;
import com.mooddiary.diary.application.port.out.PhotoRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import com.mooddiary.diary.domain.diary.Score1to10;
import com.mooddiary.diary.domain.photo.Photo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadDiaryEntryPhotosUseCaseImplTest {
    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    @Mock
    private PhotoRepositoryPort photoRepositoryPort;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private PhotoUploadProperties config;

    @InjectMocks
    private UploadDiaryEntryPhotosUseCaseImpl useCase;

    @Test
    void shouldUploadPhotosHappyPath() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();

        DiaryEntry entry = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                entryDate,
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                null,
                false,
                List.of(),
                List.of(),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60)
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.of(entry));
        when(config.getMaxFileSizeBytes()).thenReturn(1024L);

        byte[] bytes = new byte[] {1, 2, 3};
        PhotoUploadFileCommand file = new PhotoUploadFileCommand(
                "a.png",
                "image/png",
                bytes.length,
                bytes
        );

        when(fileStoragePort.save(any(byte[].class), any(String.class))).thenAnswer(inv -> inv.getArgument(1));

        when(photoRepositoryPort.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));

        UploadDiaryEntryPhotosCommand command = new UploadDiaryEntryPhotosCommand(List.of(file));

        List<PhotoResponse> responses = useCase.execute(keycloakSubject, diaryEntryId, command);

        assertEquals(1, responses.size());
        assertEquals(diaryEntryId, responses.get(0).entryId());

        ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
        verify(photoRepositoryPort).save(photoCaptor.capture());
        Photo savedPhoto = photoCaptor.getValue();
        assertEquals("a.png", savedPhoto.getFileName());
        assertEquals("image/png", savedPhoto.getContentType());
        assertEquals(bytes.length, savedPhoto.getSize());
    }

    @Test
    void shouldThrowNotFoundWhenDiaryEntryMissing() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.empty());

        byte[] bytes = new byte[] {1, 2, 3};
        PhotoUploadFileCommand file = new PhotoUploadFileCommand("a.png", "image/png", bytes.length, bytes);
        UploadDiaryEntryPhotosCommand command = new UploadDiaryEntryPhotosCommand(List.of(file));

        assertThrows(NotFoundAppException.class, () -> useCase.execute(keycloakSubject, diaryEntryId, command));
    }

    @Test
    void shouldThrowValidationWhenFileTooLarge() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();

        DiaryEntry entry = DiaryEntry.fromPersistence(
                diaryEntryId,
                userId,
                LocalDate.now(),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                Score1to10.of(1),
                null,
                false,
                List.of(),
                List.of(),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60)
        );

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);
        when(diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)).thenReturn(Optional.of(entry));
        when(config.getMaxFileSizeBytes()).thenReturn(2L);

        byte[] bytes = new byte[] {1, 2, 3};
        PhotoUploadFileCommand file = new PhotoUploadFileCommand("a.png", "image/png", bytes.length, bytes);
        UploadDiaryEntryPhotosCommand command = new UploadDiaryEntryPhotosCommand(List.of(file));

        assertThrows(ValidationAppException.class, () -> useCase.execute(keycloakSubject, diaryEntryId, command));

        verify(fileStoragePort, never()).save(any(byte[].class), any(String.class));
    }
}

