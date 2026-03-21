package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.application.port.out.UserRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class UserSettingsController {
    private final UserRepositoryPort userRepository;

    public UserSettingsController(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/telegram-chat-id")
    public ResponseEntity<Map<String, String>> getTelegramChatId(@AuthenticationPrincipal Jwt jwt) {
        String chatId = userRepository.getTelegramChatIdByKeycloakSubject(jwt.getSubject()).orElse("");
        return ResponseEntity.ok(Map.of("telegramChatId", chatId));
    }

    @PutMapping("/telegram-chat-id")
    public ResponseEntity<Void> updateTelegramChatId(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body
    ) {
        String chatId = body != null ? body.get("telegramChatId") : null;
        userRepository.updateTelegramChatId(jwt.getSubject(), chatId);
        return ResponseEntity.ok().build();
    }
}
