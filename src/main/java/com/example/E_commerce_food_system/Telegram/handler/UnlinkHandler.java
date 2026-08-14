package com.example.E_commerce_food_system.Telegram.handler;

import org.springframework.stereotype.Component;

import com.example.E_commerce_food_system.Service.TelegramLinkService;
import com.example.E_commerce_food_system.Telegram.TelegramSender;

/** /unlink -- disconnects this chat from the account. Notifications stop. */
@Component
public class UnlinkHandler implements CommandHandler {

    private final TelegramSender sender;
    private final TelegramLinkService linkService;

    public UnlinkHandler(TelegramSender sender, TelegramLinkService linkService) {
        this.sender = sender;
        this.linkService = linkService;
    }

    @Override
    public String command() {
        return "/unlink";
    }

    @Override
    public String description() {
        return "disconnect your account from this chat";
    }

    @Override
    public void handle(CommandContext ctx) {
        boolean wasLinked = linkService.unlink(ctx.chatId());

        sender.send(ctx.chatId(), wasLinked
                ? "Disconnected. I will not send you order updates any more."
                : "This chat is not connected to an account.");
    }
}
