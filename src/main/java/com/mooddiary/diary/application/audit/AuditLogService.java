package com.mooddiary.diary.application.audit;

import java.util.UUID;

public interface AuditLogService {
    void record(
            UUID performedBy,
            String actionType,
            String entityType,
            UUID entityId,
            String clientIp,
            String forwardedHeaders,
            String userAgent
    );
}

