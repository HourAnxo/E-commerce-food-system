package com.example.E_commerce_food_system.Telegram;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.example.E_commerce_food_system.Telegram.handler.CommandContext;
import com.example.E_commerce_food_system.Telegram.handler.CommandHandler;

/**
 * Entry point for everything coming from Telegram.
 *
 * This class does three things only: read the message, work out which command it is,
 * and pass it to the right handler. No business logic and no database access here --
 * exactly like a REST controller.
 *
 * The bean only exists when telegram.bot.token is set, so the rest of the app still
 * boots normally on a machine with no token configured.
 */
@Component
@ConditionalOnExpression("'${telegram.bot.token:}'.length() > 0")
public class    FoodOrderBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(FoodOrderBot.class);

    private final String botToken;
    private final TelegramSender sender;
    private final Map<String, CommandHandler> handlers;

    public FoodOrderBot(@Value("${telegram.bot.token:}") String botToken,
                        TelegramSender sender,
                        List<CommandHandler> handlerList) {
        this.botToken = botToken;
        this.sender = sender;
        // Spring injects every CommandHandler bean automatically.
        // Add a new handler class and it registers itself here -- no edit needed in this file.
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(CommandHandler::command, Function.identity()));
        log.info("Telegram bot started with commands: {}", handlers.keySet());
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return; // ignore photos, stickers, edits, joins, etc.
        }

        Long chatId = update.getMessage().getChatId();
        Long telegramUserId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String text = update.getMessage().getText().trim();

        // "/track 42" -> command = "/track", args = "42"
        String firstWord = text.split("\\s+")[0];
        String args = text.substring(firstWord.length()).trim();
        String command = firstWord.toLowerCase();

        // Telegram sends "/start@MyFoodBot" inside groups -- strip the bot username
        int at = command.indexOf('@');
        if (at > 0) {
            command = command.substring(0, at);
        }

        CommandHandler handler = handlers.get(command);

        try {
            if (handler == null) {
                sender.send(chatId, "I don't know that command. Send /help to see what I can do.");
                return;
            }
            handler.handle(new CommandContext(chatId, telegramUserId, firstName, args));
        } catch (Exception e) {
            // Never let an exception kill the polling thread.
            log.error("Failed to handle '{}' for chat {}", command, chatId, e);
            sender.send(chatId, "Something went wrong on our side. Please try again in a moment.");
        }
    }
}
