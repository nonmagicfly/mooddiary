package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.port.out.UserRepositoryPort;
import com.mooddiary.diary.infrastructure.persistence.jpa.UserJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryJpaAdapter implements UserRepositoryPort {
    private final UserJpaRepository userJpaRepository;

    public UserRepositoryJpaAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    @Transactional
    public UUID getOrCreateByKeycloakSubject(String keycloakSubject) {
        return userJpaRepository.findByKeycloakSubject(keycloakSubject)
                .map(UserJpaEntity::getId)
                .orElseGet(() -> createUser(keycloakSubject));
    }

    private UUID createUser(String keycloakSubject) {
        UserJpaEntity user = new UserJpaEntity();
        user.setId(UUID.randomUUID());
        user.setKeycloakSubject(keycloakSubject);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userJpaRepository.save(user);
        return user.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getTelegramChatIdByKeycloakSubject(String keycloakSubject) {
        return userJpaRepository.findByKeycloakSubject(keycloakSubject)
                .map(UserJpaEntity::getTelegramChatId)
                .filter(id -> id != null && !id.isBlank());
    }

    @Override
    @Transactional
    public void updateTelegramChatId(String keycloakSubject, String telegramChatId) {
        getOrCreateByKeycloakSubject(keycloakSubject);
        userJpaRepository.findByKeycloakSubject(keycloakSubject)
                .ifPresent(user -> {
                    user.setTelegramChatId(telegramChatId != null && !telegramChatId.isBlank() ? telegramChatId.trim() : null);
                    userJpaRepository.save(user);
                });
    }
}

