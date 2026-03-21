package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.application.audit.AuditLogEvent;
import com.mooddiary.diary.infrastructure.persistence.jpa.AuditLogJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.AuditLogJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryJpaAdapterTest {
    @Mock
    private AuditLogJpaRepository auditLogJpaRepository;

    @Test
    void shouldPersistAuditLogEvent() {
        AuditLogRepositoryJpaAdapter adapter = new AuditLogRepositoryJpaAdapter(auditLogJpaRepository);

        AuditLogEvent event = new AuditLogEvent(
                UUID.randomUUID(),
                "CREATE",
                "DiaryEntry",
                UUID.randomUUID(),
                Instant.now(),
                "1.2.3.4",
                "X-Forwarded-For=1.2.3.4",
                "ua"
        );

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        when(auditLogJpaRepository.save(any(AuditLogJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(event);

        verify(auditLogJpaRepository).save(captor.capture());
        AuditLogJpaEntity saved = captor.getValue();
        assertEquals(event.performedBy(), saved.getPerformedBy());
        assertEquals(event.actionType(), saved.getActionType());
        assertEquals(event.entityType(), saved.getEntityType());
        assertEquals(event.entityId(), saved.getEntityId());
        assertEquals(event.occurredAt(), saved.getOccurredAt());
        assertEquals(event.clientIp(), saved.getClientIp());
        assertEquals(event.forwardedHeaders(), saved.getForwardedHeaders());
        assertEquals(event.userAgent(), saved.getUserAgent());
        assertEquals(null, saved.getId());
    }
}

