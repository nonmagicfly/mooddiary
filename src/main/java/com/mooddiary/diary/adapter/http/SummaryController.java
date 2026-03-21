package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.application.summary.SendDailySummaryToTelegramUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/summary")
public class SummaryController {
    private final SendDailySummaryToTelegramUseCase sendDailySummaryToTelegramUseCase;

    public SummaryController(SendDailySummaryToTelegramUseCase sendDailySummaryToTelegramUseCase) {
        this.sendDailySummaryToTelegramUseCase = sendDailySummaryToTelegramUseCase;
    }

    @PostMapping("/send-telegram")
    public ResponseEntity<Void> sendToTelegram(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        sendDailySummaryToTelegramUseCase.execute(jwt.getSubject(), targetDate);
        return ResponseEntity.ok().build();
    }
}
