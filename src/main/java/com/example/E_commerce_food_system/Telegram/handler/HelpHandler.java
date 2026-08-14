package com.example.E_commerce_food_system.Telegram.handler;

import java.util.Comparator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.example.E_commerce_food_system.Telegram.TelegramSender;

/**
 * Builds the help text from the handlers themselves, so it can never go stale.
 *
 * The handler list is resolved through an ObjectProvider instead of being injected
 * directly: this handler is itself in that list, so a direct injection would be a
 * circular reference. ObjectProvider defers the lookup until /help is actually run.
 */
@Component
public class HelpHandler implements CommandHandler {

    private final TelegramSender sender;
    private final ObjectProvider<CommandHandler> allHandlers;

    public HelpHandler(TelegramSender sender, ObjectProvider<CommandHandler> allHandlers) {
        this.sender = sender;
        this.allHandlers = allHandlers;
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "show this message";
    }

    @Override
    public void handle(CommandContext ctx) {
        StringBuilder sb = new StringBuilder("Here is what I can do:\n\n");
        allHandlers.orderedStream()
                .sorted(Comparator.comparing(CommandHandler::command))
                .forEach(h -> sb.append(h.command()).append(" - ").append(h.description()).append("\n"));

        sender.send(ctx.chatId(), sb.toString());
    }
}
