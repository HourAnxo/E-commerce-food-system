package com.example.E_commerce_food_system.Service;

import java.util.Optional;

import com.example.E_commerce_food_system.DTO.CustomerDTO;

/**
 * Binds a Telegram chat to a customer account.
 *
 * The bot handlers are the only callers today, but this deliberately lives in the
 * Service layer so the handlers stay as thin as controllers and never touch a
 * repository directly.
 */
public interface TelegramLinkService {

    /** Verifies the password and binds the chat. Throws 401 if the credentials are wrong. */
    CustomerDTO link(Long chatId, String email, String password);

    /** Unbinds the chat. Returns false if it was not linked in the first place. */
    boolean unlink(Long chatId);

    /** The account this chat belongs to, or empty when the chat is not linked. */
    Optional<CustomerDTO> findByChatId(Long chatId);

    /** The chat to notify for a customer, or empty when they never linked one. */
    Optional<Long> findChatIdByCustomerId(Integer customerId);
}
