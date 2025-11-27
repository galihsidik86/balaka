package com.artivisi.accountingfinance.controller;

import com.artivisi.accountingfinance.config.TelegramConfig;
import com.artivisi.accountingfinance.service.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);

    private final TelegramBotService telegramBotService;
    private final TelegramConfig telegramConfig;

    public TelegramWebhookController(TelegramBotService telegramBotService, TelegramConfig telegramConfig) {
        this.telegramBotService = telegramBotService;
        this.telegramConfig = telegramConfig;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> onUpdate(
            @RequestBody Update update,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken) {

        // Validate secret token if configured
        String configuredSecret = telegramConfig.getWebhook().getSecretToken();
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            if (secretToken == null || !secretToken.equals(configuredSecret)) {
                log.warn("Invalid secret token in webhook request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid secret token");
            }
        }

        log.debug("Received Telegram update: {}", update.getUpdateId());

        try {
            telegramBotService.handleUpdate(update);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error handling Telegram update", e);
            // Return 200 to prevent Telegram from retrying
            return ResponseEntity.ok("Error handled");
        }
    }
}
