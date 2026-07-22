package com.mooddiary.diary.application.photo.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.photo.PhotoResponse;
import com.mooddiary.diary.application.photo.PhotoUploadFileCommand;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosCommand;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosUseCase;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.port.out.FileStoragePort;
import com.mooddiary.diary.application.port.out.PhotoRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.domain.photo.Photo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UploadDiaryEntryPhotosUseCaseImpl implements UploadDiaryEntryPhotosUseCase {
    private final UserIdentityService userIdentityService;
    private final DiaryEntryRepositoryPort diaryEntryRepositoryPort;
    private final PhotoRepositoryPort photoRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final PhotoUploadProperties config;

    public UploadDiaryEntryPhotosUseCaseImpl(
            UserIdentityService userIdentityService,
            DiaryEntryRepositoryPort diaryEntryRepositoryPort,
            PhotoRepositoryPort photoRepositoryPort,
            FileStoragePort fileStoragePort,
            PhotoUploadProperties config
    ) {
        this.userIdentityService = userIdentityService;
        this.diaryEntryRepositoryPort = diaryEntryRepositoryPort;
        this.photoRepositoryPort = photoRepositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.config = config;
    }

    @Override
    @Transactional
    public List<PhotoResponse> execute(String keycloakSubject, UUID diaryEntryId, UploadDiaryEntryPhotosCommand command) {
        if (diaryEntryId == null) {
            throw new ValidationAppException("diaryEntryId is required");
        }
        if (command == null || command.files() == null || command.files().isEmpty()) {
            throw new ValidationAppException("files are required");
        }

        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        var entry = diaryEntryRepositoryPort.findByIdAndUserId(diaryEntryId, userId)
                .orElseThrow(() -> new NotFoundAppException("Diary entry not found"));

        List<PhotoResponse> responses = new ArrayList<>();
        for (PhotoUploadFileCommand file : command.files()) {
            responses.add(uploadSingle(userId, entry.getId(), file));
        }
        return responses;
    }

    private PhotoResponse uploadSingle(UUID userId, UUID diaryEntryId, PhotoUploadFileCommand file) {
        if (file == null) {
            throw new ValidationAppException("file is required");
        }
        if (file.content() == null || file.content().length == 0) {
            throw new ValidationAppException("file content is required");
        }
        if (file.size() <= 0) {
            throw new ValidationAppException("file size is invalid");
        }
        if (file.size() > config.getMaxFileSizeBytes()) {
            throw new ValidationAppException("Файл слишком большой. Попробуйте фото меньшего размера.");
        }
        if (file.contentType() == null || !file.contentType().startsWith("image/")) {
            throw new ValidationAppException("file contentType must be image/*");
        }
        String originalName = sanitizeOriginalName(file.originalFileName());
        String storageRelative = buildRelativePath(userId, diaryEntryId, originalName);

        String savedRelative = fileStoragePort.save(file.content(), storageRelative);
        try {
            Instant now = Instant.now();
            Photo photo = new Photo(
                    UUID.randomUUID(),
                    diaryEntryId,
                    originalName,
                    savedRelative,
                    file.contentType(),
                    file.size(),
                    now
            );
            Photo saved = photoRepositoryPort.save(photo);
            return new PhotoResponse(
                    saved.getId(),
                    saved.getEntryId(),
                    saved.getFileName(),
                    saved.getFilePath(),
                    saved.getContentType(),
                    saved.getSize(),
                    saved.getCreatedAt()
            );
        } catch (RuntimeException ex) {
            fileStoragePort.delete(savedRelative);
            throw ex;
        }
    }

    private String sanitizeOriginalName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "upload.bin";
        }
        String clean = originalFileName.replaceAll("[\\\\/]", "_");
        clean = clean.replaceAll("\\.\\.+", ".");
        return clean;
    }

    private String buildRelativePath(UUID userId, UUID diaryEntryId, String originalName) {
        String storedName = UUID.randomUUID() + "_" + originalName;
        return "photos/" + userId + "/" + diaryEntryId + "/" + storedName;
    }
}

