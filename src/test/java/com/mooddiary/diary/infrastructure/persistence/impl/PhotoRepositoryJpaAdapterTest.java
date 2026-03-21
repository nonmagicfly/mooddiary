package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.PhotoRepositoryPort;
import com.mooddiary.diary.domain.photo.Photo;
import com.mooddiary.diary.infrastructure.persistence.jpa.PhotoJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.PhotoJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PhotoRepositoryJpaAdapterTest {
    @Mock
    private PhotoJpaRepository photoJpaRepository;

    @Test
    void shouldSaveMapping() {
        PhotoRepositoryPort adapter = new PhotoRepositoryJpaAdapter(photoJpaRepository);

        UUID photoId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        Photo domain = new Photo(
                photoId,
                entryId,
                "a.png",
                "photos/u/e/a.png",
                "image/png",
                3L,
                createdAt
        );

        PhotoJpaEntity savedEntity = new PhotoJpaEntity();
        savedEntity.setId(photoId);
        savedEntity.setEntryId(entryId);
        savedEntity.setFileName("a.png");
        savedEntity.setFilePath("photos/u/e/a.png");
        savedEntity.setContentType("image/png");
        savedEntity.setSize(3L);
        savedEntity.setCreatedAt(createdAt);

        when(photoJpaRepository.save(any(PhotoJpaEntity.class))).thenReturn(savedEntity);

        Photo result = adapter.save(domain);

        assertEquals(photoId, result.getId());
        assertEquals(entryId, result.getEntryId());
        assertEquals("a.png", result.getFileName());
        assertEquals("photos/u/e/a.png", result.getFilePath());
        assertEquals("image/png", result.getContentType());
        assertEquals(3L, result.getSize());
        assertEquals(createdAt, result.getCreatedAt());
    }

    @Test
    void shouldDeleteByIdAndUserId() {
        PhotoRepositoryPort adapter = new PhotoRepositoryJpaAdapter(photoJpaRepository);
        UUID photoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(photoJpaRepository.deleteByIdAndDiaryEntryUserId(photoId, userId)).thenReturn(1);

        boolean deleted = adapter.deleteByIdAndUserId(photoId, userId);
        assertEquals(true, deleted);
        verify(photoJpaRepository).deleteByIdAndDiaryEntryUserId(photoId, userId);
    }

    @Test
    void shouldFindByIdAndUserIdMapping() {
        PhotoRepositoryPort adapter = new PhotoRepositoryJpaAdapter(photoJpaRepository);
        UUID photoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PhotoJpaEntity entity = new PhotoJpaEntity();
        entity.setId(photoId);
        entity.setEntryId(UUID.randomUUID());
        entity.setFileName("a.png");
        entity.setFilePath("photos/u/e/a.png");
        entity.setContentType("image/png");
        entity.setSize(3L);
        entity.setCreatedAt(Instant.now());

        when(photoJpaRepository.findByIdAndDiaryEntryUserId(photoId, userId)).thenReturn(Optional.of(entity));

        Photo result = adapter.findByIdAndUserId(photoId, userId).orElseThrow();
        assertEquals(photoId, result.getId());
        assertEquals("a.png", result.getFileName());
    }
}

