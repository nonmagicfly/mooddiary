package com.mooddiary.diary.infrastructure.persistence.jpa.repository;

import com.mooddiary.diary.infrastructure.persistence.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByKeycloakSubject(String keycloakSubject);
}

