package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.audit.AuditLogEvent;
import com.mooddiary.diary.application.port.out.AuditLogRepositoryPort;
import com.mooddiary.diary.infrastructure.persistence.jpa.AuditLogJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.AuditLogJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepositoryJpaAdapter implements AuditLogRepositoryPort {
    private final AuditLogJpaRepository auditLogJpaRepository;

    public AuditLogRepositoryJpaAdapter(AuditLogJpaRepository auditLogJpaRepository) {
        this.auditLogJpaRepository = auditLogJpaRepository;
    }

    @Override
    public void save(AuditLogEvent event) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setPerformedBy(event.performedBy());
        entity.setActionType(event.actionType());
        entity.setEntityType(event.entityType());
        entity.setEntityId(event.entityId());
        entity.setOccurredAt(event.occurredAt());
        entity.setClientIp(event.clientIp());
        entity.setForwardedHeaders(event.forwardedHeaders());
        entity.setUserAgent(event.userAgent());
        auditLogJpaRepository.save(entity);
    }
}

