package com.example.E_commerce_food_system.Telegram.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.E_commerce_food_system.DTO.CustomerDTO;
import com.example.E_commerce_food_system.Service.TelegramLinkService;
import com.example.E_commerce_food_system.Telegram.TelegramSender;

/**
 * /link email password -- proves who you are, so /track can show your orders and
 * the bot can notify you when a driver picks one up.
 *
 * The password arrives as plain text in a chat message, so the message is deleted
 * immediately afterwards. That is not perfect (Telegram's servers saw it, and it
 * is only best effort), but it beats leaving credentials in the scrollback.
 */
@Component
public class LinkHandler implements CommandHandler {

    private final TelegramSender sender;
    private final TelegramLinkService linkService;

    public LinkHandler(TelegramSender sender, TelegramLinkService linkService) {
        this.sender = sender;
        this.linkService = linkService;
    }

    @Override
    public String command() {
        return "/link";
    }

    @Override
    public String description() {
        return "connect your account: /link <email> <password>";
    }

    @Override
    public void handle(CommandContext ctx) {
        // Delete first: whatever happens next, the password should not linger.
        sender.deleteMessage(ctx.chatId(), ctx.messageId());

        String[] parts = ctx.hasArgs() ? ctx.args().split("\\s+", 2) : new String[0];
        if (parts.length < 2) {
            sender.send(ctx.chatId(),
                    "Send it as: /link your@email.com yourpassword\n\n"
                    + "I delete that message straight away so your password does not stay in the chat.");
            return;
        }

        CustomerDTO customer;
        try {
            customer = linkService.link(ctx.chatId(), parts[0].trim(), parts[1]);
        } catch (ResponseStatusException e) {
            sender.send(ctx.chatId(), "That email and password did not match an account.");
            return;
        }

        sender.send(ctx.chatId(),
                "Linked to " + customer.getEmail() + ". "
                + "Send /track to see your orders. Use /unlink to disconnect.");
    }
}
