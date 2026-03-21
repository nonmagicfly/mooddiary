package com.mooddiary.diary.application.usecase.impl;

import com.mooddiary.diary.application.diary.DiaryEntryListQuery;
import com.mooddiary.diary.application.diary.DiaryEntryResponse;
import com.mooddiary.diary.application.exception.ValidationAppException;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import com.mooddiary.diary.application.usecase.ListDiaryEntriesUseCase;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ListDiaryEntriesUseCaseImpl implements ListDiaryEntriesUseCase {
    private static final int MAX_LIMIT = 200;

    private final UserIdentityService userIdentityService;
    private final DiaryEntryRepositoryPort diaryEntryRepositoryPort;

    public ListDiaryEntriesUseCaseImpl(UserIdentityService userIdentityService, DiaryEntryRepositoryPort diaryEntryRepositoryPort) {
        this.userIdentityService = userIdentityService;
        this.diaryEntryRepositoryPort = diaryEntryRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntryResponse> execute(String keycloakSubject, DiaryEntryListQuery query) {
        Objects.requireNonNull(query, "query");
        if (query.limit() < 1 || query.limit() > MAX_LIMIT) {
            throw new ValidationAppException("limit must be within 1..200");
        }
        UUID userId = userIdentityService.getOrCreateUserId(keycloakSubject);
        List<DiaryEntry> entries = diaryEntryRepositoryPort.findByUserId(
                userId,
                query.from(),
                query.to(),
                query.limit()
        );
        return entries.stream().map(this::map).toList();
    }

    private DiaryEntryResponse map(DiaryEntry entry) {
        return new DiaryEntryResponse(
                entry.getUserId(),
                entry.getId(),
                entry.getEntryDate(),
                entry.getMoodScore().value(),
                entry.getEnergyScore().value(),
                entry.getProductivityScore().value(),
                entry.getStressScore().value(),
                entry.getSleepQualityScore().value(),
                entry.getNote(),
                entry.isCompleted(),
                entry.getTagIds(),
                entry.getSymptomIds(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}

