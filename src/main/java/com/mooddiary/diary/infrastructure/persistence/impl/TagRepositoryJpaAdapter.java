package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.TagRepositoryPort;
import com.mooddiary.diary.domain.tag.Tag;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.TagJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class TagRepositoryJpaAdapter implements TagRepositoryPort {
    private final TagJpaRepository tagJpaRepository;

    public TagRepositoryJpaAdapter(TagJpaRepository tagJpaRepository) {
        this.tagJpaRepository = tagJpaRepository;
    }

    @Override
    public boolean existsByIdAndUserId(UUID tagId, UUID userId) {
        return tagJpaRepository.existsByIdAndUserId(tagId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndName(UUID userId, String name) {
        return tagJpaRepository.existsByUserIdAndName(userId, name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllByUserId(UUID userId) {
        return tagJpaRepository.findByUserIdOrderByNameAsc(userId).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> findByIdAndUserId(UUID tagId, UUID userId) {
        return tagJpaRepository.findByIdAndUserId(tagId, userId).map(this::mapToDomain);
    }

    @Override
    @Transactional
    public Tag save(Tag tag) {
        var entity = mapToEntity(tag);
        var saved = tagJpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    @Transactional
    public boolean deleteByIdAndUserId(UUID tagId, UUID userId) {
        return tagJpaRepository.deleteByIdAndUserId(tagId, userId) > 0;
    }

    private Tag mapToDomain(com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity entity) {
        return new Tag(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getColor(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity mapToEntity(Tag tag) {
        com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity entity = new com.mooddiary.diary.infrastructure.persistence.jpa.TagJpaEntity();
        entity.setId(tag.getId());
        entity.setUserId(tag.getUserId());
        entity.setName(tag.getName());
        entity.setColor(tag.getColor());
        if (tag.getCreatedAt() != null) {
            entity.setCreatedAt(tag.getCreatedAt());
        }
        if (tag.getUpdatedAt() != null) {
            entity.setUpdatedAt(tag.getUpdatedAt());
        }
        return entity;
    }
}

