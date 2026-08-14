package com.example.E_commerce_food_system.Telegram.handler;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import com.example.E_commerce_food_system.Entity.Delivery.DeliveryStatus;
import com.example.E_commerce_food_system.Service.DeliveryService;
import com.example.E_commerce_food_system.Telegram.TelegramSender;

/**
 * /track 42  -- looks up the delivery attached to that order id.
 *
 * Note: this deliberately does NOT echo the 6-digit delivery_code or the courier's
 * phone number. That code is the secret the customer reads out to the driver, and
 * there is no auth on Telegram side, so anyone could /track someone else's order.
 */
@Component
public class TrackOrderHandler implements CommandHandler {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM, HH:mm");

    private final TelegramSender sender;
    private final DeliveryService deliveryService;

    public TrackOrderHandler(TelegramSender sender, DeliveryService deliveryService) {
        this.sender = sender;
        this.deliveryService = deliveryService;
    }

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "check the status of your order";
    }

    @Override
    public void handle(CommandContext ctx) {
        if (!ctx.hasArgs()) {
            sender.send(ctx.chatId(), "Please send your order number too, like this:\n/track 42");
            return;
        }

        Integer orderId;
        try {
            orderId = Integer.valueOf(ctx.args().trim());
        } catch (NumberFormatException e) {
            sender.send(ctx.chatId(), "\"" + ctx.args() + "\" is not an order number. Try /track 42");
            return;
        }

        DeliveryDTO delivery;
        try {
            delivery = deliveryService.getDeliveryByOrderId(orderId);
        } catch (ResponseStatusException e) {
            sender.send(ctx.chatId(), "I couldn't find a delivery for order " + orderId + ".");
            return;
        }

        StringBuilder sb = new StringBuilder("*Order ").append(orderId).append("*\n\n");
        sb.append("Status: ").append(humanStatus(delivery.getDeliveryStatus())).append("\n");

        if (delivery.getDeliveryPerson() != null && !delivery.getDeliveryPerson().isBlank()) {
            sb.append("Courier: ").append(delivery.getDeliveryPerson()).append("\n");
        }
        if (delivery.getEstimatedDelivery() != null) {
            sb.append("Estimated: ").append(delivery.getEstimatedDelivery().format(WHEN)).append("\n");
        }
        if (delivery.getDeliveredAt() != null) {
            sb.append("Delivered: ").append(delivery.getDeliveredAt().format(WHEN)).append("\n");
        }

        sender.sendMarkdown(ctx.chatId(), sb.toString());
    }

    /** Turns the DeliveryStatus enum into something a customer understands. */
    private String humanStatus(DeliveryStatus status) {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case Preparing -> "Being prepared";
            case Assigned  -> "A courier has been offered the job";
            case Shipped   -> "On the way to you";
            case Delivered -> "Delivered - please confirm in the app";
            case Completed -> "Completed";
            case Disputed  -> "A problem was reported, we are looking into it";
        };
    }
}
