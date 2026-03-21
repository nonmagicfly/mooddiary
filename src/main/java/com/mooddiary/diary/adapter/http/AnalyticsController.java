package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.analytics.GetAnalyticsForPeriodUseCase;
import com.mooddiary.diary.application.analytics.MoodAnalyticsResponse;
import com.mooddiary.diary.application.service.UserIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private static final String ENTITY_TYPE = "Analytics";

    private final GetAnalyticsForPeriodUseCase getAnalyticsForPeriodUseCase;
    private final AuditLogService auditLogService;
    private final ClientRequestMetadataResolver requestMetadataResolver;
    private final UserIdentityService userIdentityService;

    public AnalyticsController(
            GetAnalyticsForPeriodUseCase getAnalyticsForPeriodUseCase,
            AuditLogService auditLogService,
            ClientRequestMetadataResolver requestMetadataResolver,
            UserIdentityService userIdentityService
    ) {
        this.getAnalyticsForPeriodUseCase = getAnalyticsForPeriodUseCase;
        this.auditLogService = auditLogService;
        this.requestMetadataResolver = requestMetadataResolver;
        this.userIdentityService = userIdentityService;
    }

    @GetMapping("/daily")
    public ResponseEntity<MoodAnalyticsResponse> daily(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        LocalDate d = date == null ? LocalDate.now() : date;
        return ok(jwt, httpRequest, d, d);
    }

    @GetMapping("/weekly")
    public ResponseEntity<MoodAnalyticsResponse> weekly(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        LocalDate d = date == null ? LocalDate.now() : date;
        LocalDate from = d.minusDays(6);
        LocalDate to = d;
        return ok(jwt, httpRequest, from, to);
    }

    @GetMapping("/monthly")
    public ResponseEntity<MoodAnalyticsResponse> monthly(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        LocalDate d = date == null ? LocalDate.now() : date;
        LocalDate from = d.withDayOfMonth(1);
        LocalDate to = d.withDayOfMonth(d.lengthOfMonth());
        return ok(jwt, httpRequest, from, to);
    }

    private ResponseEntity<MoodAnalyticsResponse> ok(Jwt jwt, HttpServletRequest httpRequest, LocalDate from, LocalDate to) {
        MoodAnalyticsResponse response = getAnalyticsForPeriodUseCase.execute(jwt.getSubject(), from, to);
        var performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        audit(performedBy, "READ", performedBy, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private void audit(java.util.UUID performedBy, String actionType, java.util.UUID entityId, HttpServletRequest httpRequest) {
        var metadata = requestMetadataResolver.resolve(httpRequest);
        auditLogService.record(
                performedBy,
                actionType,
                ENTITY_TYPE,
                entityId,
                metadata.clientIp(),
                metadata.forwardedHeaders(),
                metadata.userAgent()
        );
    }
}

