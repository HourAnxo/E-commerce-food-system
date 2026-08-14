package com.example.E_commerce_food_system.Telegram.handler;

public record CommandContext(
        Long chatId,
        Long telegramUserId,
        String firstName,
        String args
) {
    public boolean hasArgs() {
        return args != null && !args.isBlank();
    }
}
