package com.example.E_commerce_food_system.Telegram.handler;

/**
 * @param messageId id of the message that triggered the command. /link uses it to
 *                  delete the message afterwards so the password does not sit in
 *                  the chat history.
 */
public record CommandContext(
        Long chatId,
        Long telegramUserId,
        String firstName,
        String args,
        Integer messageId
) {
    public boolean hasArgs() {
        return args != null && !args.isBlank();
    }
}
