package com.mooddiary.diary.application.port.out;

import com.mooddiary.diary.application.audit.AuditLogEvent;

public interface AuditLogRepositoryPort {

    void save(AuditLogEvent event);
}
