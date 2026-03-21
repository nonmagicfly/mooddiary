package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.PhotoRepositoryPort;
import com.mooddiary.diary.domain.photo.Photo;
import com.mooddiary.diary.infrastructure.persistence.jpa.PhotoJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.PhotoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PhotoRepositoryJpaAdapter implements PhotoRepositoryPort {
    private final PhotoJpaRepository photoJpaRepository;

    public PhotoRepositoryJpaAdapter(PhotoJpaRepository photoJpaRepository) {
        this.photoJpaRepository = photoJpaRepository;
    }

    @Override
    @Transactional
    public Photo save(Photo photo) {
        PhotoJpaEntity entity = mapToEntity(photo);
        PhotoJpaEntity saved = photoJpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Photo> findByIdAndUserId(UUID photoId, UUID userId) {
        return photoJpaRepository.findByIdAndDiaryEntryUserId(photoId, userId).map(this::mapToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Photo> findByDiaryEntryIdAndUserId(UUID diaryEntryId, UUID userId) {
        return photoJpaRepository.findByDiaryEntryIdAndUserId(diaryEntryId, userId).stream().map(this::mapToDomain).toList();
    }

    @Override
    @Transactional
    public boolean deleteByIdAndUserId(UUID photoId, UUID userId) {
        int deleted = photoJpaRepository.deleteByIdAndDiaryEntryUserId(photoId, userId);
        return deleted > 0;
    }

    private Photo mapToDomain(PhotoJpaEntity entity) {
        return new Photo(
                entity.getId(),
                entity.getEntryId(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getContentType(),
                entity.getSize(),
                entity.getCreatedAt()
        );
    }

    private PhotoJpaEntity mapToEntity(Photo photo) {
        PhotoJpaEntity entity = new PhotoJpaEntity();
        entity.setId(photo.getId());
        entity.setEntryId(photo.getEntryId());
        entity.setFileName(photo.getFileName());
        entity.setFilePath(photo.getFilePath());
        entity.setContentType(photo.getContentType());
        entity.setSize(photo.getSize());
        entity.setCreatedAt(photo.getCreatedAt());
        return entity;
    }
}

