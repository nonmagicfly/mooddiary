package com.mooddiary.diary.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    UUID getOrCreateByKeycloakSubject(String keycloakSubject);

    Optional<String> getTelegramChatIdByKeycloakSubject(String keycloakSubject);

    void updateTelegramChatId(String keycloakSubject, String telegramChatId);
}
