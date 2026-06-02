package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.application.analytics.AnalyticsCorrelations;
import com.mooddiary.diary.application.analytics.GetAnalyticsForPeriodUseCase;
import com.mooddiary.diary.application.analytics.MoodAnalyticsResponse;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {
    @Mock
    private GetAnalyticsForPeriodUseCase getAnalyticsForPeriodUseCase;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ClientRequestMetadataResolver requestMetadataResolver;

    @Mock
    private UserIdentityService userIdentityService;

    @InjectMocks
    private AnalyticsController controller;

    @Test
    void shouldCallUseCaseForDailyAndAudit() {
        String keycloakSubject = "sub-1";
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 3, 10);

        Jwt jwt = new Jwt("token", date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC), date.atStartOfDay().plusSeconds(3600).toInstant(java.time.ZoneOffset.UTC), Map.of("alg", "none"), Map.of("sub", keycloakSubject));

        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);

        MoodAnalyticsResponse response = new MoodAnalyticsResponse(
                date,
                date,
                1.0,
                2.0,
                3.0,
                1,
                List.of(),
                new AnalyticsCorrelations(null),
                List.of()
        );
        when(getAnalyticsForPeriodUseCase.execute(keycloakSubject, date, date)).thenReturn(response);

        var httpRequest = new MockHttpServletRequest();
        var metadata = new com.mooddiary.diary.adapter.http.audit.RequestAuditMetadata("1.2.3.4", "", "ua");
        when(requestMetadataResolver.resolve(httpRequest)).thenReturn(metadata);

        ResponseEntity<MoodAnalyticsResponse> result = controller.daily(jwt, date, httpRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());

        verify(auditLogService).record(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq("READ"),
                ArgumentMatchers.eq("Analytics"),
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq(metadata.clientIp()),
                ArgumentMatchers.eq(metadata.forwardedHeaders()),
                ArgumentMatchers.eq(metadata.userAgent())
        );
    }
}

