package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.adapter.http.audit.RequestAuditMetadata;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.photo.DeletePhotoUseCase;
import com.mooddiary.diary.application.photo.PhotoResponse;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosCommand;
import com.mooddiary.diary.application.photo.UploadDiaryEntryPhotosUseCase;
import com.mooddiary.diary.application.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PhotoControllerTest {
    @Mock
    private UploadDiaryEntryPhotosUseCase uploadDiaryEntryPhotosUseCase;

    @Mock
    private DeletePhotoUseCase deletePhotoUseCase;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ClientRequestMetadataResolver requestMetadataResolver;

    @Mock
    private UserIdentityService userIdentityService;

    @Test
    void shouldUploadPhotosAndAudit() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        String keycloakSubject = "sub-1";

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", keycloakSubject));

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "X-Forwarded-For=1.2.3.4", "ua");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(httpRequest)).thenReturn(metadata);
        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);

        MultipartFile file = new MockMultipartFile("files", "a.png", "image/png", new byte[] {1, 2, 3});

        UUID photoId = UUID.randomUUID();
        PhotoResponse photoResponse = new PhotoResponse(
                photoId,
                diaryEntryId,
                "a.png",
                "photos/u/d/" + photoId,
                "image/png",
                3L,
                Instant.now()
        );

        when(uploadDiaryEntryPhotosUseCase.execute(
                ArgumentMatchers.eq(keycloakSubject),
                ArgumentMatchers.eq(diaryEntryId),
                ArgumentMatchers.any(UploadDiaryEntryPhotosCommand.class)
        )).thenReturn(List.of(photoResponse));

        PhotoController controller = new PhotoController(
                uploadDiaryEntryPhotosUseCase,
                deletePhotoUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        var result = controller.upload(jwt, diaryEntryId, List.of(file), httpRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(photoId, result.getBody().get(0).id());

        verify(auditLogService).record(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq("CREATE"),
                ArgumentMatchers.eq("Photo"),
                ArgumentMatchers.eq(photoId),
                ArgumentMatchers.eq(metadata.clientIp()),
                ArgumentMatchers.eq(metadata.forwardedHeaders()),
                ArgumentMatchers.eq(metadata.userAgent())
        );
    }

    @Test
    void shouldDeletePhotoAndAudit() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";
        UUID photoId = UUID.randomUUID();

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", keycloakSubject));
        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "", "ua");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(httpRequest)).thenReturn(metadata);

        when(deletePhotoUseCase.execute(keycloakSubject, photoId)).thenReturn(photoId);

        PhotoController controller = new PhotoController(
                uploadDiaryEntryPhotosUseCase,
                deletePhotoUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        var result = controller.delete(jwt, photoId, httpRequest);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(auditLogService).record(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq("DELETE"),
                ArgumentMatchers.eq("Photo"),
                ArgumentMatchers.eq(photoId),
                ArgumentMatchers.eq(metadata.clientIp()),
                ArgumentMatchers.eq(metadata.forwardedHeaders()),
                ArgumentMatchers.eq(metadata.userAgent())
        );
    }
}

