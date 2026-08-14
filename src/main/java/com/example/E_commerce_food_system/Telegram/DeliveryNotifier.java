package com.example.E_commerce_food_system.Telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.E_commerce_food_system.Service.DeliveryAcceptedEvent;
import com.example.E_commerce_food_system.Service.TelegramLinkService;

/**
 * Pushes delivery updates to the customer's Telegram chat.
 *
 * This is the half the bot could not do before: /track is pull-only, and without a
 * chat linked to an account there was nowhere to send anything.
 *
 * AFTER_COMMIT matters — on the default BEFORE_COMMIT/inline behaviour a later
 * rollback would still have left the customer told their food was on the way.
 */
@Component
public class DeliveryNotifier {

    private static final Logger log = LoggerFactory.getLogger(DeliveryNotifier.class);

    private final TelegramSender sender;
    private final TelegramLinkService linkService;

    public DeliveryNotifier(TelegramSender sender, TelegramLinkService linkService) {
        this.sender = sender;
        this.linkService = linkService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeliveryAccepted(DeliveryAcceptedEvent event) {
        // Most customers never link a chat; that is the normal case, not an error.
        linkService.findChatIdByCustomerId(event.customerId()).ifPresent(chatId -> {
            StringBuilder sb = new StringBuilder();
            sb.append("🛵 *Order ").append(event.orderId()).append(" is on the way!*\n\n");

            if (event.deliveryPerson() != null && !event.deliveryPerson().isBlank()) {
                sb.append("Courier: ").append(event.deliveryPerson()).append("\n");
            }
            if (event.deliveryCode() != null && !event.deliveryCode().isBlank()) {
                sb.append("\nGive the driver this code when they arrive: *")
                  .append(event.deliveryCode()).append("*");
            }

            sender.sendMarkdown(chatId, sb.toString());
            log.info("Notified customer {} about order {}", event.customerId(), event.orderId());
        });
    }
}
