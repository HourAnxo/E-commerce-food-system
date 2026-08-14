package com.example.E_commerce_food_system.Telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * The only class that actually talks to the Telegram API.
 *
 * Handlers call sender.send(chatId, text) and don't worry about clients or
 * checked exceptions. Any other Spring bean can inject this too -- that is how
 * you will later push "your order has shipped" notifications from DeliveryService.
 */
@Component
public class TelegramSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramSender.class);

    private final TelegramClient client;

    public TelegramSender(@Value("${telegram.bot.token:}") String botToken) {
        this.client = new OkHttpTelegramClient(botToken);
    }

    public void send(Long chatId, String text) {
        execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build());
    }

    /** Same as send(), but lets you use *bold* and _italic_ formatting. */
    public void sendMarkdown(Long chatId, String text) {
        execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build());
    }

    /**
     * Best effort. Telegram refuses this for messages older than 48h, and in groups
     * the bot needs delete rights -- neither is worth surfacing to the user.
     */
    public void deleteMessage(Long chatId, Integer messageId) {
        if (messageId == null) {
            return;
        }
        try {
            client.execute(DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Could not delete message {} in chat {}: {}", messageId, chatId, e.getMessage());
        }
    }

    private void execute(SendMessage message) {
        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            // Common cause: the user blocked the bot. Log it, don't crash.
            log.warn("Could not send message to chat {}: {}", message.getChatId(), e.getMessage());
        }
    }
}
