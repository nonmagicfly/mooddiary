package com.mooddiary.diary.application.audit.impl;

import com.mooddiary.diary.application.audit.AuditLogEvent;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.port.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public AuditLogServiceImpl(AuditLogRepositoryPort auditLogRepositoryPort) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    public void record(
            UUID performedBy,
            String actionType,
            String entityType,
            UUID entityId,
            String clientIp,
            String forwardedHeaders,
            String userAgent
    ) {
        AuditLogEvent event = new AuditLogEvent(
                performedBy,
                actionType,
                entityType,
                entityId,
                Instant.now(),
                clientIp,
                forwardedHeaders,
                userAgent
        );
        auditLogRepositoryPort.save(event);
    }
}

