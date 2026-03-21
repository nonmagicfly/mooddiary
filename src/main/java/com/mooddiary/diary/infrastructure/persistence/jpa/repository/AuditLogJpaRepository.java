package com.mooddiary.diary.infrastructure.persistence.jpa.repository;

import com.mooddiary.diary.infrastructure.persistence.jpa.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {
}

