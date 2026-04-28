package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.adapter.http.audit.ClientRequestMetadataResolver;
import com.mooddiary.diary.adapter.http.audit.RequestAuditMetadata;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryControllerTest {
    @Mock
    private CreateDiaryEntryUseCase createDiaryEntryUseCase;

    @Mock
    private UpdateDiaryEntryUseCase updateDiaryEntryUseCase;

    @Mock
    private GetDiaryEntryUseCase getDiaryEntryUseCase;

    @Mock
    private ListDiaryEntriesUseCase listDiaryEntriesUseCase;

    @Mock
    private DeleteDiaryEntryUseCase deleteDiaryEntryUseCase;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ClientRequestMetadataResolver requestMetadataResolver;

    @Mock
    private UserIdentityService userIdentityService;

    @Captor
    private ArgumentCaptor<DiaryEntryCreateCommand> createCommandCaptor;

    @Captor
    private ArgumentCaptor<DiaryEntryListQuery> listQueryCaptor;

    @Test
    void shouldCreateAndAuditDiaryEntry() {
        UUID userId = UUID.randomUUID();
        UUID diaryEntryId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now();
        UUID tagId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("sub", "sub-1")
        );

        DiaryEntryResponse response = new DiaryEntryResponse(
                userId,
                diaryEntryId,
                entryDate,
                1,
                2,
                3,
                4,
                5,
                "note",
                false,
                Set.of(tagId),
                Set.of(symptomId),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60)
        );

        when(createDiaryEntryUseCase.execute(any(String.class), any(DiaryEntryCreateCommand.class))).thenReturn(response);

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "X-Forwarded-For=1.2.3.4", "ua");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(servletRequest)).thenReturn(metadata);

        DiaryEntryController controller = new DiaryEntryController(
                createDiaryEntryUseCase,
                updateDiaryEntryUseCase,
                getDiaryEntryUseCase,
                listDiaryEntriesUseCase,
                deleteDiaryEntryUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        DiaryEntryUpsertRequest request = new DiaryEntryUpsertRequest(
                entryDate,
                1,
                2,
                3,
                4,
                5,
                "note",
                false,
                Set.of(tagId),
                Set.of(symptomId)
        );

        controller.create(jwt, request, servletRequest);

        verify(createDiaryEntryUseCase).execute(any(String.class), createCommandCaptor.capture());
        assertEquals(entryDate, createCommandCaptor.getValue().entryDate());
        assertEquals(1, createCommandCaptor.getValue().moodScore());
        assertEquals(Set.of(tagId), createCommandCaptor.getValue().tagIds());
        assertEquals(Set.of(symptomId), createCommandCaptor.getValue().symptomIds());

        verify(auditLogService).record(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.eq("CREATE"),
                org.mockito.ArgumentMatchers.eq("DiaryEntry"),
                org.mockito.ArgumentMatchers.eq(diaryEntryId),
                org.mockito.ArgumentMatchers.eq(metadata.clientIp()),
                org.mockito.ArgumentMatchers.eq(metadata.forwardedHeaders()),
                org.mockito.ArgumentMatchers.eq(metadata.userAgent())
        );
    }

    @Test
    void shouldListAndAudit() {
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        int limit = 10;

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("sub", keycloakSubject)
        );

        DiaryEntryResponse entry = new DiaryEntryResponse(
                userId,
                UUID.randomUUID(),
                to,
                1,
                1,
                1,
                1,
                1,
                null,
                false,
                Set.of(),
                Set.of(),
                Instant.now(),
                Instant.now()
        );

        when(listDiaryEntriesUseCase.execute(any(String.class), any(DiaryEntryListQuery.class))).thenReturn(List.of(entry));
        when(userIdentityService.getOrCreateUserId(keycloakSubject)).thenReturn(userId);

        RequestAuditMetadata metadata = new RequestAuditMetadata("1.2.3.4", "", "ua");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        when(requestMetadataResolver.resolve(servletRequest)).thenReturn(metadata);

        DiaryEntryController controller = new DiaryEntryController(
                createDiaryEntryUseCase,
                updateDiaryEntryUseCase,
                getDiaryEntryUseCase,
                listDiaryEntriesUseCase,
                deleteDiaryEntryUseCase,
                auditLogService,
                requestMetadataResolver,
                userIdentityService
        );

        controller.list(jwt, from, to, limit, servletRequest);

        verify(listDiaryEntriesUseCase).execute(any(String.class), listQueryCaptor.capture());
        assertEquals(from, listQueryCaptor.getValue().from());
        assertEquals(to, listQueryCaptor.getValue().to());
        assertEquals(limit, listQueryCaptor.getValue().limit());

        verify(auditLogService).record(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.eq("LIST"),
                org.mockito.ArgumentMatchers.eq("DiaryEntry"),
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(String.class),
                org.mockito.ArgumentMatchers.any(String.class),
                org.mockito.ArgumentMatchers.any(String.class)
        );
    }
}

