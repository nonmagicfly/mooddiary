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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/symptoms")
public class SymptomController {
    private static final String ENTITY_TYPE = "Symptom";

    private final CreateSymptomUseCase createSymptomUseCase;
    private final ListSymptomsUseCase listSymptomsUseCase;
    private final UpdateSymptomUseCase updateSymptomUseCase;
    private final DeleteSymptomUseCase deleteSymptomUseCase;
    private final AuditLogService auditLogService;
    private final ClientRequestMetadataResolver requestMetadataResolver;
    private final UserIdentityService userIdentityService;

    public SymptomController(
            CreateSymptomUseCase createSymptomUseCase,
            ListSymptomsUseCase listSymptomsUseCase,
            UpdateSymptomUseCase updateSymptomUseCase,
            DeleteSymptomUseCase deleteSymptomUseCase,
            AuditLogService auditLogService,
            ClientRequestMetadataResolver requestMetadataResolver,
            UserIdentityService userIdentityService
    ) {
        this.createSymptomUseCase = createSymptomUseCase;
        this.listSymptomsUseCase = listSymptomsUseCase;
        this.updateSymptomUseCase = updateSymptomUseCase;
        this.deleteSymptomUseCase = deleteSymptomUseCase;
        this.auditLogService = auditLogService;
        this.requestMetadataResolver = requestMetadataResolver;
        this.userIdentityService = userIdentityService;
    }

    @PostMapping
    public ResponseEntity<SymptomResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SymptomUpsertRequest request,
            HttpServletRequest httpRequest
    ) {
        SymptomCreateCommand cmd = new SymptomCreateCommand(request.name());
        SymptomResponse response = createSymptomUseCase.execute(jwt.getSubject(), cmd);
        audit(response.userId(), "CREATE", response.id(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SymptomResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest
    ) {
        UUID performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        List<SymptomResponse> response = listSymptomsUseCase.execute(jwt.getSubject());
        audit(performedBy, "LIST", performedBy, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SymptomResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            @Valid @RequestBody SymptomUpsertRequest request,
            HttpServletRequest httpRequest
    ) {
        SymptomUpdateCommand cmd = new SymptomUpdateCommand(request.name());
        SymptomResponse response = updateSymptomUseCase.execute(jwt.getSubject(), id, cmd);
        audit(response.userId(), "UPDATE", response.id(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            HttpServletRequest httpRequest
    ) {
        UUID deletedId = deleteSymptomUseCase.execute(jwt.getSubject(), id);
        UUID performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        audit(performedBy, "DELETE", deletedId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    private void audit(UUID performedBy, String actionType, UUID entityId, HttpServletRequest httpRequest) {
        RequestAuditMetadata metadata = requestMetadataResolver.resolve(httpRequest);
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

