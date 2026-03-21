package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.photo.DeletePhotoUseCase;
import com.mooddiary.diary.application.photo.PhotoResponse;
import com.mooddiary.diary.application.photo.PhotoUploadFileCommand;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosCommand;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosUseCase;
import com.mooddiary.diary.application.service.UserIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PhotoController {
    private static final String ENTITY_TYPE = "Photo";

    private final UploadDiaryEntryPhotosUseCase uploadDiaryEntryPhotosUseCase;
    private final DeletePhotoUseCase deletePhotoUseCase;
    private final AuditLogService auditLogService;
    private final ClientRequestMetadataResolver requestMetadataResolver;
    private final UserIdentityService userIdentityService;

    public PhotoController(
            UploadDiaryEntryPhotosUseCase uploadDiaryEntryPhotosUseCase,
            DeletePhotoUseCase deletePhotoUseCase,
            AuditLogService auditLogService,
            ClientRequestMetadataResolver requestMetadataResolver,
            UserIdentityService userIdentityService
    ) {
        this.uploadDiaryEntryPhotosUseCase = uploadDiaryEntryPhotosUseCase;
        this.deletePhotoUseCase = deletePhotoUseCase;
        this.auditLogService = auditLogService;
        this.requestMetadataResolver = requestMetadataResolver;
        this.userIdentityService = userIdentityService;
    }

    @PostMapping(value = "/diary-entries/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PhotoResponse>> upload(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID diaryEntryId,
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest httpRequest
    ) throws IOException {
        UUID performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        List<PhotoUploadFileCommand> uploadFiles = toCommands(files);
        UploadDiaryEntryPhotosCommand command = new UploadDiaryEntryPhotosCommand(uploadFiles);

        List<PhotoResponse> saved = uploadDiaryEntryPhotosUseCase.execute(jwt.getSubject(), diaryEntryId, command);
        for (PhotoResponse photo : saved) {
            audit(performedBy, "CREATE", photo.id(), httpRequest);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID photoId,
            HttpServletRequest httpRequest
    ) {
        UUID performedBy = userIdentityService.getOrCreateUserId(jwt.getSubject());
        UUID deletedId = deletePhotoUseCase.execute(jwt.getSubject(), photoId);
        audit(performedBy, "DELETE", deletedId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    private List<PhotoUploadFileCommand> toCommands(List<MultipartFile> files) throws IOException {
        List<PhotoUploadFileCommand> commands = new ArrayList<>();
        if (files == null) {
            return commands;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String originalName = file.getOriginalFilename();
            String contentType = file.getContentType();
            long size = file.getSize();
            byte[] content = file.getBytes();
            commands.add(new PhotoUploadFileCommand(originalName, contentType, size, content));
        }
        return commands;
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
}

