package com.mooddiary.diary.application.port.out;

import com.mooddiary.diary.domain.tag.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepositoryPort {

    boolean existsByIdAndUserId(UUID tagId, UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    List<Tag> findAllByUserId(UUID userId);

    Optional<Tag> findByIdAndUserId(UUID tagId, UUID userId);

    Tag save(Tag tag);

    boolean deleteByIdAndUserId(UUID tagId, UUID userId);
}
