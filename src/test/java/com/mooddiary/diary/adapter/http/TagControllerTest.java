package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.adapter.http.audit.RequestAuditMetadata;
import com.mooddiary.diary.application.audit.AuditLogService;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.tag.TagCreateCommand;
import com.mooddiary.diary.application.tag.TagResponse;
import com.mooddiary.diary.application.tag.TagUpdateCommand;
import com.mooddiary.diary.application.tag.usecase.CreateTagUseCase;
import com.mooddiary.diary.application.tag.usecase.DeleteTagUseCase;
import com.mooddiary.diary.application.tag.usecase.ListTagsUseCase;
import com.mooddiary.diary.application.tag.usecase.UpdateTagUseCase;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {
    @Mock
    private CreateTagUseCase createTagUseCase;

    @Mock
    private ListTagsUseCase listTagsUseCase;

    @Mock
    private UpdateTagUseCase updateTagUseCase;

    @Mock
    private DeleteTagUseCase deleteTagUseCase;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ClientRequestMetadataResolver requestMetadataResolver;

    @Mock
    private UserIdentityService userIdentityService;

    @Test
    void shouldCreateTagAndAudit() {
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        String keycloakSubject = "sub-1";

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", keycloakSubject));
        TagResponse response = new TagResponse(userId, tagId, "work", "red", Instant.now(), Instant.now());

        when(createTagUseCase.execute(ArgumentMatchers.eq(keycloakSubject), ArgumentMatchers.any(TagCreateCommand.class))).thenReturn(response);

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "X-Forwarded-For=1.2.3.4", "ua");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(servletRequest)).thenReturn(metadata);

        TagController controller = new TagController(
                createTagUseCase,
                listTagsUseCase,
                updateTagUseCase,
                deleteTagUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        TagController controllerImpl = controller;
        ResponseEntity<TagResponse> result = controllerImpl.create(jwt, new TagUpsertRequest("work", "red"), servletRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(tagId, result.getBody().id());

        verify(auditLogService).record(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq("CREATE"),
                ArgumentMatchers.eq("Tag"),
                ArgumentMatchers.eq(tagId),
                ArgumentMatchers.eq(metadata.clientIp()),
                ArgumentMatchers.eq(metadata.forwardedHeaders()),
                ArgumentMatchers.eq(metadata.userAgent())
        );
    }

    @Test
    void shouldListTagsAndAudit() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", keycloakSubject));
        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);

        TagResponse response = new TagResponse(userId, UUID.randomUUID(), "work", null, Instant.now(), null);
        when(listTagsUseCase.execute(keycloakSubject)).thenReturn(List.of(response));

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "", "ua");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(servletRequest)).thenReturn(metadata);

        TagController controller = new TagController(
                createTagUseCase,
                listTagsUseCase,
                updateTagUseCase,
                deleteTagUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        ResponseEntity<List<TagResponse>> result = controller.list(jwt, servletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());

        verify(auditLogService).record(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq("LIST"),
                ArgumentMatchers.eq("Tag"),
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq(metadata.clientIp()),
                ArgumentMatchers.eq(metadata.forwardedHeaders()),
                ArgumentMatchers.eq(metadata.userAgent())
        );
    }
}

