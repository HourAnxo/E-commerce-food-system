package com.example.E_commerce_food_system.Telegram.handler;

import org.springframework.stereotype.Component;

import com.example.E_commerce_food_system.Telegram.TelegramSender;

/**
 * /start is sent automatically the first time someone opens your bot.
 */
@Component
public class StartHandler implements CommandHandler {

    private final TelegramSender sender;

    public StartHandler(TelegramSender sender) {
        this.sender = sender;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "start using the bot";
    }

    @Override
    public void handle(CommandContext ctx) {
        String text = """
                Hello %s! 👋

                I can help you track your food orders.

                Send /track followed by your order number, for example:
                /track 42

                Send /help to see everything I can do.
                """.formatted(ctx.firstName());

        sender.send(ctx.chatId(), text);
    }
}
