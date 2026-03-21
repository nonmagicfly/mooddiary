package com.mooddiary.diary.adapter.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TelegramClient {
    private static final String TELEGRAM_API = "https://api.telegram.org";

    private final RestClient restClient = RestClient.create();
    private final String botToken;

    public TelegramClient(@Value("${TELEGRAM_BOT_TOKEN:}") String botToken) {
        this.botToken = botToken != null ? botToken.trim() : "";
    }

    public boolean isConfigured() {
        return !botToken.isEmpty();
    }

    public void sendMessage(String chatId, String text) {
        if (!isConfigured()) {
            throw new IllegalStateException("TELEGRAM_BOT_TOKEN is not configured");
        }
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("Telegram chat ID is required");
        }

        String url = TELEGRAM_API + "/bot" + botToken + "/sendMessage";
        var body = new SendMessageRequest(chatId.trim(), text);

        var response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(SendMessageResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getBody().ok()) {
            throw new RuntimeException("Telegram API error: " + (response.getBody() != null ? response.getBody().description() : response.getStatusCode()));
        }
    }

    private record SendMessageRequest(String chat_id, String text) {}

    private record SendMessageResponse(boolean ok, String description) {}
}
