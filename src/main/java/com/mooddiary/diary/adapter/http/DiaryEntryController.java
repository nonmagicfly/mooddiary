package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.diary.DiaryEntryCreateCommand;
import com.mooddiary.diary.application.diary.DiaryEntryDeleteResult;
import com.mooddiary.diary.application.diary.DiaryEntryListQuery;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.diary.DiaryEntryUpdateCommand;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.usecase.CreateDiaryEntryUseCase;
import com.mooddiary.diary.application.usecase.DeleteDiaryEntryUseCase;
import com.mooddiary.diary.application.usecase.GetDiaryEntryUseCase;
import com.mooddiary.diary.application.usecase.ListDiaryEntriesUseCase;
import com.mooddiary.diary.application.usecase.UpdateDiaryEntryUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mooddiary.diary.domain.diary.DiaryEntryLockRules;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diary-entries")
public class DiaryEntryController {
    private static final String ENTITY_TYPE = "DiaryEntry";

    private final CreateDiaryEntryUseCase createDiaryEntryUseCase;
    private final UpdateDiaryEntryUseCase updateDiaryEntryUseCase;
    private final GetDiaryEntryUseCase getDiaryEntryUseCase;
    private final ListDiaryEntriesUseCase listDiaryEntriesUseCase;
    private final DeleteDiaryEntryUseCase deleteDiaryEntryUseCase;
    private final AuditLogService auditLogService;
    private final ClientRequestMetadataResolver requestMetadataResolver;
    private final UserIdentityService userIdentityService;

    public DiaryEntryController(
            CreateDiaryEntryUseCase createDiaryEntryUseCase,
            UpdateDiaryEntryUseCase updateDiaryEntryUseCase,
            GetDiaryEntryUseCase getDiaryEntryUseCase,
            ListDiaryEntriesUseCase listDiaryEntriesUseCase,
            DeleteDiaryEntryUseCase deleteDiaryEntryUseCase,
            AuditLogService auditLogService,
            ClientRequestMetadataResolver requestMetadataResolver,
            UserIdentityService userIdentityService
    ) {
        this.createDiaryEntryUseCase = createDiaryEntryUseCase;
        this.updateDiaryEntryUseCase = updateDiaryEntryUseCase;
        this.getDiaryEntryUseCase = getDiaryEntryUseCase;
        this.listDiaryEntriesUseCase = listDiaryEntriesUseCase;
        this.deleteDiaryEntryUseCase = deleteDiaryEntryUseCase;
        this.auditLogService = auditLogService;
        this.requestMetadataResolver = requestMetadataResolver;
        this.userIdentityService = userIdentityService;
    }

    @PostMapping
    public ResponseEntity<DiaryEntryResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DiaryEntryUpsertRequest request,
            HttpServletRequest httpRequest
    ) {
        DiaryEntryCreateCommand command = toCreateCommand(request);
        DiaryEntryResponse response = createDiaryEntryUseCase.execute(jwt.getSubject(), command);
        audit(response.userId(), "CREATE", response.id(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryEntryResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            @Valid @RequestBody DiaryEntryUpsertRequest request,
            HttpServletRequest httpRequest
    ) {
        DiaryEntryUpdateCommand command = toUpdateCommand(request);
        DiaryEntryResponse response = updateDiaryEntryUseCase.execute(jwt.getSubject(), id, command);
        audit(response.userId(), "UPDATE", response.id(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaryEntryResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            HttpServletRequest httpRequest
    ) {
        DiaryEntryResponse response = getDiaryEntryUseCase.execute(jwt.getSubject(), id);
        audit(response.userId(), "READ", response.id(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DiaryEntryResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "30") int limit,
            HttpServletRequest httpRequest
    ) {
        List<DiaryEntryResponse> entries = listDiaryEntriesUseCase.execute(
                jwt.getSubject(),
                new DiaryEntryListQuery(from, to, limit)
        );

        UUID performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        audit(performedBy, "LIST", performedBy, httpRequest);
        return ResponseEntity.ok(entries);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            HttpServletRequest httpRequest
    ) {
        DiaryEntryDeleteResult result = deleteDiaryEntryUseCase.execute(jwt.getSubject(), id);
        audit(result.userId(), "DELETE", result.diaryEntryId(), httpRequest);
        return ResponseEntity.noContent().build();
    }

    private void audit(UUID performedBy, String actionType, UUID entityId, HttpServletRequest httpRequest) {
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

    private DiaryEntryCreateCommand toCreateCommand(DiaryEntryUpsertRequest request) {
        boolean locked = DiaryEntryLockRules.isEditLocked(request.entryDate(), LocalDate.now());
        return new DiaryEntryCreateCommand(
                request.entryDate(),
                request.moodScore(),
                request.energyScore(),
                request.productivityScore(),
                request.stressScore(),
                request.sleepQualityScore(),
                request.note(),
                locked,
                request.tagIds(),
                request.symptomIds()
        );
    }

    private DiaryEntryUpdateCommand toUpdateCommand(DiaryEntryUpsertRequest request) {
        boolean locked = DiaryEntryLockRules.isEditLocked(request.entryDate(), LocalDate.now());
        return new DiaryEntryUpdateCommand(
                request.entryDate(),
                request.moodScore(),
                request.energyScore(),
                request.productivityScore(),
                request.stressScore(),
                request.sleepQualityScore(),
                request.note(),
                locked,
                request.tagIds(),
                request.symptomIds()
        );
    }
}

