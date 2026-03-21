package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.adapter.http.audit.RequestAuditMetadata;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.symptom.SymptomCreateCommand;
import com.mooddiary.diary.application.symptom.SymptomResponse;
import com.mooddiary.diary.application.symptom.SymptomUpdateCommand;
import com.mooddiary.diary.application.symptom.usecase.CreateSymptomUseCase;
import com.mooddiary.diary.application.symptom.usecase.DeleteSymptomUseCase;
import com.mooddiary.diary.application.symptom.usecase.ListSymptomsUseCase;
import com.mooddiary.diary.application.symptom.usecase.UpdateSymptomUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SymptomControllerTest {
    @Mock
    private CreateSymptomUseCase createSymptomUseCase;

    @Mock
    private ListSymptomsUseCase listSymptomsUseCase;

    @Mock
    private UpdateSymptomUseCase updateSymptomUseCase;

    @Mock
    private DeleteSymptomUseCase deleteSymptomUseCase;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ClientRequestMetadataResolver requestMetadataResolver;

    @Mock
    private UserIdentityService userIdentityService;

    @Test
    void shouldCreateSymptomAndAudit() {
        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        String keycloakSubject = "sub-1";

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", keycloakSubject));
        SymptomResponse response = new SymptomResponse(userId, symptomId, "pain", Instant.now(), Instant.now());

        when(createSymptomUseCase.execute(ArgumentMatchers.eq(keycloakSubject), ArgumentMatchers.any(SymptomCreateCommand.class))).thenReturn(response);

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "", "ua");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(servletRequest)).thenReturn(metadata);

        SymptomController controller = new SymptomController(
                createSymptomUseCase,
                listSymptomsUseCase,
                updateSymptomUseCase,
                deleteSymptomUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        ResponseEntity<SymptomResponse> result = controller.create(jwt, new SymptomUpsertRequest("pain"), servletRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(symptomId, result.getBody().id());

        verify(auditLogService).record(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq("CREATE"),
                ArgumentMatchers.eq("Symptom"),
                ArgumentMatchers.eq(symptomId),
                ArgumentMatchers.eq(metadata.clientIp()),
                ArgumentMatchers.eq(metadata.forwardedHeaders()),
                ArgumentMatchers.eq(metadata.userAgent())
        );
    }
}

