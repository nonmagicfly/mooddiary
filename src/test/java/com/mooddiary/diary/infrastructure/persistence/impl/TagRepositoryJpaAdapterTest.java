package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.domain.tag.Tag;
import com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.TagJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TagRepositoryJpaAdapterTest {
    @Mock
    private TagJpaRepository tagJpaRepository;

    @Test
    void shouldDelegateExistsByIdAndUserId() {
        TagRepositoryJpaAdapter adapter = new TagRepositoryJpaAdapter(tagJpaRepository);

        UUID tagId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(tagJpaRepository.existsByIdAndUserId(tagId, userId)).thenReturn(true);

        assertEquals(true, adapter.existsByIdAndUserId(tagId, userId));
    }

    @Test
    void shouldDelegateExistsByUserIdAndName() {
        TagRepositoryJpaAdapter adapter = new TagRepositoryJpaAdapter(tagJpaRepository);

        UUID userId = UUID.randomUUID();
        when(tagJpaRepository.existsByUserIdAndName(userId, "work")).thenReturn(false);

        assertEquals(false, adapter.existsByUserIdAndName(userId, "work"));
    }

    @Test
    void shouldFindAllByUserIdMapping() {
        TagRepositoryJpaAdapter adapter = new TagRepositoryJpaAdapter(tagJpaRepository);

        UUID userId = UUID.randomUUID();
        TagJpaEntity e1 = new TagJpaEntity();
        e1.setId(UUID.randomUUID());
        e1.setUserId(userId);
        e1.setName("work");
        e1.setColor("red");
        e1.setCreatedAt(Instant.now().minusSeconds(3600));
        e1.setUpdatedAt(Instant.now().minusSeconds(60));

        when(tagJpaRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(e1));

        List<Tag> result = adapter.findAllByUserId(userId);
        assertEquals(1, result.size());
        assertEquals("work", result.get(0).getName());
        assertEquals("red", result.get(0).getColor());
    }

    @Test
    void shouldSaveMapping() {
        TagRepositoryJpaAdapter adapter = new TagRepositoryJpaAdapter(tagJpaRepository);

        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        Tag domain = new Tag(tagId, userId, "work", "red", createdAt, updatedAt);

        TagJpaEntity savedEntity = new TagJpaEntity();
        savedEntity.setId(tagId);
        savedEntity.setUserId(userId);
        savedEntity.setName("work");
        savedEntity.setColor("red");
        savedEntity.setCreatedAt(createdAt);
        savedEntity.setUpdatedAt(updatedAt);

        when(tagJpaRepository.save(any(TagJpaEntity.class))).thenReturn(savedEntity);

        Tag result = adapter.save(domain);

        ArgumentCaptor<TagJpaEntity> captor = ArgumentCaptor.forClass(TagJpaEntity.class);
        verify(tagJpaRepository).save(captor.capture());

        assertEquals(tagId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals("work", result.getName());
        assertEquals("red", result.getColor());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void shouldDelegateDeleteByIdAndUserId() {
        TagRepositoryJpaAdapter adapter = new TagRepositoryJpaAdapter(tagJpaRepository);

        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        when(tagJpaRepository.deleteByIdAndUserId(tagId, userId)).thenReturn(1L);

        assertEquals(true, adapter.deleteByIdAndUserId(tagId, userId));
    }
}

