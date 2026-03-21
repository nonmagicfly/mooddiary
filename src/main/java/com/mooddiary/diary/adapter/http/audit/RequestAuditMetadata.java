package com.mooddiary.diary.adapter.http.audit;

public record RequestAuditMetadata(
        String clientIp,
        String forwardedHeaders,
        String userAgent
) {
}

