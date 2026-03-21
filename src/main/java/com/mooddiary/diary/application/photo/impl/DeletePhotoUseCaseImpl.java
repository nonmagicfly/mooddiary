package com.mooddiary.diary.application.photo.impl;

import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.FileStoragePort;
import com.mooddiary.diary.application.port.out.PhotoRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.photo.DeletePhotoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeletePhotoUseCaseImpl implements DeletePhotoUseCase {
    private final UserIdentityService userIdentityService;
    private final PhotoRepositoryPort photoRepositoryPort;
    private final FileStoragePort fileStoragePort;

    public DeletePhotoUseCaseImpl(
            UserIdentityService userIdentityService,
            PhotoRepositoryPort photoRepositoryPort,
            FileStoragePort fileStoragePort
    ) {
        this.userIdentityService = userIdentityService;
        this.photoRepositoryPort = photoRepositoryPort;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    @Transactional
    public UUID execute(String keycloakSubject, UUID photoId) {
        if (photoId == null) {
            throw new ValidationAppException("photoId is required");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);

        var photoOpt = photoRepositoryPort.findByIdAndUserId(photoId, userId);
        var photo = photoOpt.orElseThrow(() -> new NotFoundAppException("Photo not found"));

        boolean deleted = photoRepositoryPort.deleteByIdAndUserId(photoId, userId);
        if (!deleted) {
            throw new NotFoundAppException("Photo not found");
        }

        fileStoragePort.delete(photo.getFilePath());
        return photoId;
    }
}

