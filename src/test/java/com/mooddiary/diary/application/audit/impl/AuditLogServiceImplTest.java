package com.mooddiary.diary.application.audit.impl;

import com.mooddiary.diary.application.audit.AuditLogEvent;
import com.mooddiary.diary.application.port.out.AuditLogRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {
    @Mock
    private AuditLogRepositoryPort auditLogRepositoryPort;

    @Test
    void shouldRecordAuditLogEvent() {
        AuditLogServiceImpl service = new AuditLogServiceImpl(auditLogRepositoryPort);

        UUID performedBy = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        String actionType = "CREATE";
        String entityType = "DiaryEntry";
        String clientIp = "1.2.3.4";
        String forwardedHeaders = "X-Forwarded-For: 1.2.3.4";
        String userAgent = "agent";

        Instant before = Instant.now();
        service.record(performedBy, actionType, entityType, entityId, clientIp, forwardedHeaders, userAgent);
        Instant after = Instant.now();

        ArgumentCaptor<AuditLogEvent> captor = ArgumentCaptor.forClass(AuditLogEvent.class);
        verify(auditLogRepositoryPort).save(captor.capture());

        AuditLogEvent event = captor.getValue();
        assertEquals(performedBy, event.performedBy());
        assertEquals(actionType, event.actionType());
        assertEquals(entityType, event.entityType());
        assertEquals(entityId, event.entityId());
        assertNotNull(event.occurredAt());
        assertEquals(clientIp, event.clientIp());
        assertEquals(forwardedHeaders, event.forwardedHeaders());
        assertEquals(userAgent, event.userAgent());

        boolean occurredInWindow = !event.occurredAt().isBefore(before) && !event.occurredAt().isAfter(after);
        assertEquals(true, occurredInWindow);
    }
}

