package com.mooddiary.diary.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLogJpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "entity_type", nullable = false, length = 128)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "forwarded_headers")
    private String forwardedHeaders;

    @Column(name = "user_agent")
    private String userAgent;

    @PrePersist
    public void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getPerformedBy() {
        return performedBy;
    }

    public String getActionType() {
        return actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getForwardedHeaders() {
        return forwardedHeaders;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setPerformedBy(UUID performedBy) {
        this.performedBy = performedBy;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setForwardedHeaders(String forwardedHeaders) {
        this.forwardedHeaders = forwardedHeaders;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}

