package com.mooddiary.diary.application.summary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mooddiary.diary.adapter.telegram.TelegramClient;
import com.mooddiary.diary.application.port.out.DiaryEntryRepositoryPort;
import com.mooddiary.diary.application.port.out.UserRepositoryPort;
import com.mooddiary.diary.domain.diary.DiaryEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SendDailySummaryToTelegramUseCase {
    private final UserRepositoryPort userRepository;
    private final DiaryEntryRepositoryPort diaryEntryRepository;
    private final TelegramClient telegramClient;
    private final ObjectMapper objectMapper;

    public SendDailySummaryToTelegramUseCase(
            UserRepositoryPort userRepository,
            DiaryEntryRepositoryPort diaryEntryRepository,
            TelegramClient telegramClient,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.diaryEntryRepository = diaryEntryRepository;
        this.telegramClient = telegramClient;
        this.objectMapper = objectMapper;
    }

    public void execute(String keycloakSubject, LocalDate date) {
        if (!telegramClient.isConfigured()) {
            throw new IllegalStateException("Telegram bot is not configured (TELEGRAM_BOT_TOKEN)");
        }

        String chatId = userRepository.getTelegramChatIdByKeycloakSubject(keycloakSubject)
                .orElseThrow(() -> new IllegalArgumentException("Telegram chat ID is not set. Configure it in Settings."));

        List<DiaryEntry> entries = diaryEntryRepository.findByUserId(
                userRepository.getOrCreateByKeycloakSubject(keycloakSubject),
                date,
                date,
                1
        );

        DiaryEntry entry = entries.isEmpty() ? null : entries.get(0);

        Map<String, Object> summary = buildSummary(date, entry);
        String json;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize summary", e);
        }

        String message = "📋 MoodDiary — саммари за " + date + "\n\n" + json;
        telegramClient.sendMessage(chatId, message);
    }

    private Map<String, Object> buildSummary(LocalDate date, DiaryEntry entry) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date.toString());
        summary.put("hasEntry", entry != null);

        if (entry != null) {
            summary.put("moodScore", entry.getMoodScore().value());
            summary.put("energyScore", entry.getEnergyScore().value());
            summary.put("productivityScore", entry.getProductivityScore().value());
            summary.put("stressScore", entry.getStressScore().value());
            summary.put("sleepQualityScore", entry.getSleepQualityScore().value());
            summary.put("isCompleted", entry.isCompleted());
            summary.put("note", entry.getNote());
            summary.put("tagIds", entry.getTagIds().stream().map(UUID::toString).toList());
            summary.put("symptomIds", entry.getSymptomIds().stream().map(UUID::toString).toList());
        }

        return summary;
    }
}
