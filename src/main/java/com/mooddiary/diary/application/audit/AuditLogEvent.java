package com.mooddiary.diary.application.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLogEvent(
        UUID performedBy,
        String actionType,
        String entityType,
        UUID entityId,
        Instant occurredAt,
        String clientIp,
        String forwardedHeaders,
        String userAgent
) {
}

