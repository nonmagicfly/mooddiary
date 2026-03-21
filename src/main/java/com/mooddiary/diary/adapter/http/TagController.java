package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.TagCreateCommand;
import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.application.tag.TagUpdateCommand;
import com.mooddiary.diary.application.tag.usecase.CreateTagUseCase;
import com.mooddiary.diary.application.tag.usecase.DeleteTagUseCase;
import com.mooddiary.diary.application.tag.usecase.ListTagsUseCase;
import com.mooddiary.diary.application.tag.usecase.UpdateTagUseCase;
import com.mooddiary.diary.adapter.http.audit.RequestAuditMetadata;
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
@RequestMapping("/api/v1/tags")
public class TagController {
    private static final String ENTITY_TYPE = "Tag";

    private final CreateTagUseCase createTagUseCase;
    private final ListTagsUseCase listTagsUseCase;
    private final UpdateTagUseCase updateTagUseCase;
    private final DeleteTagUseCase deleteTagUseCase;
    private final AuditLogService auditLogService;
    private final ClientRequestMetadataResolver requestMetadataResolver;
    private final UserIdentityService userIdentityService;

    public TagController(
            CreateTagUseCase createTagUseCase,
            ListTagsUseCase listTagsUseCase,
            UpdateTagUseCase updateTagUseCase,
            DeleteTagUseCase deleteTagUseCase,
            AuditLogService auditLogService,
            ClientRequestMetadataResolver requestMetadataResolver,
            UserIdentityService userIdentityService
    ) {
        this.createTagUseCase = createTagUseCase;
        this.listTagsUseCase = listTagsUseCase;
        this.updateTagUseCase = updateTagUseCase;
        this.deleteTagUseCase = deleteTagUseCase;
        this.auditLogService = auditLogService;
        this.requestMetadataResolver = requestMetadataResolver;
        this.userIdentityService = userIdentityService;
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TagUpsertRequest request,
            HttpServletRequest httpRequest
    ) {
        TagCreateCommand cmd = new TagCreateCommand(request.name(), request.color());
        TagResponse response = createTagUseCase.execute(jwt.getSubject(), cmd);
        audit(response.userId(), "CREATE", response.id(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest
    ) {
        UUID performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        List<TagResponse> response = listTagsUseCase.execute(jwt.getSubject());
        audit(performedBy, "LIST", performedBy, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            @Valid @RequestBody TagUpsertRequest request,
            HttpServletRequest httpRequest
    ) {
        TagUpdateCommand cmd = new TagUpdateCommand(request.name(), request.color());
        TagResponse response = updateTagUseCase.execute(jwt.getSubject(), id, cmd);
        audit(response.userId(), "UPDATE", response.id(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID id,
            HttpServletRequest httpRequest
    ) {
        UUID deletedId = deleteTagUseCase.execute(jwt.getSubject(), id);
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

