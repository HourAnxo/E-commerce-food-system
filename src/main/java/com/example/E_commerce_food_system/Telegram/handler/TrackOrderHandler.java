package com.example.E_commerce_food_system.Telegram.handler;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.E_commerce_food_system.DTO.CustomerDTO;
import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import com.example.E_commerce_food_system.DTO.OrderDTO;
import com.example.E_commerce_food_system.Entity.Delivery.DeliveryStatus;
import com.example.E_commerce_food_system.Service.DeliveryService;
import com.example.E_commerce_food_system.Service.OrderService;
import com.example.E_commerce_food_system.Service.TelegramLinkService;
import com.example.E_commerce_food_system.Telegram.TelegramSender;

/**
 * /track          -- lists your recent orders
 * /track 42       -- detail for one of your orders
 *
 * Requires a linked account (/link). Telegram carries no authentication of its own,
 * so without the link anyone could walk the order ids and read other people's
 * deliveries. Every lookup here is filtered by the linked customer id.
 */
@Component
public class TrackOrderHandler implements CommandHandler {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM, HH:mm");
    private static final int MAX_LISTED = 10;

    private final TelegramSender sender;
    private final TelegramLinkService linkService;
    private final OrderService orderService;
    private final DeliveryService deliveryService;

    public TrackOrderHandler(TelegramSender sender,
                             TelegramLinkService linkService,
                             OrderService orderService,
                             DeliveryService deliveryService) {
        this.sender = sender;
        this.linkService = linkService;
        this.orderService = orderService;
        this.deliveryService = deliveryService;
    }

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "see your orders, or /track <order number> for one";
    }

    @Override
    public void handle(CommandContext ctx) {
        Optional<CustomerDTO> linked = linkService.findByChatId(ctx.chatId());
        if (linked.isEmpty()) {
            sender.send(ctx.chatId(),
                    "Connect your account first:\n/link your@email.com yourpassword");
            return;
        }
        CustomerDTO customer = linked.get();

        if (!ctx.hasArgs()) {
            listOrders(ctx, customer);
            return;
        }

        Integer orderId;
        try {
            orderId = Integer.valueOf(ctx.args().trim());
        } catch (NumberFormatException e) {
            sender.send(ctx.chatId(), "\"" + ctx.args() + "\" is not an order number. Try /track 42");
            return;
        }

        showOrder(ctx, customer, orderId);
    }

    private void listOrders(CommandContext ctx, CustomerDTO customer) {
        List<OrderDTO> orders = orderService.getOrdersByCustomerId(customer.getCustomerId());
        if (orders.isEmpty()) {
            sender.send(ctx.chatId(), "You have no orders yet.");
            return;
        }

        StringBuilder sb = new StringBuilder("*Your orders*\n\n");
        orders.stream()
                .sorted(Comparator.comparing(OrderDTO::getOrderId).reversed())
                .limit(MAX_LISTED)
                .forEach(o -> sb.append("#").append(o.getOrderId())
                        .append(" — ").append(o.getOrderStatus())
                        .append("\n"));
        sb.append("\nSend /track ").append(orders.get(0).getOrderId()).append(" for details.");

        sender.sendMarkdown(ctx.chatId(), sb.toString());
    }

    private void showOrder(CommandContext ctx, CustomerDTO customer, Integer orderId) {
        OrderDTO order;
        try {
            order = orderService.getOrderById(orderId);
        } catch (ResponseStatusException e) {
            sender.send(ctx.chatId(), "I couldn't find order " + orderId + ".");
            return;
        }

        // The ownership check. Same message as "not found" on purpose: a different
        // reply would confirm that someone else's order id exists.
        if (!customer.getCustomerId().equals(order.getCustomerId())) {
            sender.send(ctx.chatId(), "I couldn't find order " + orderId + ".");
            return;
        }

        StringBuilder sb = new StringBuilder("*Order ").append(orderId).append("*\n\n");
        sb.append("Order status: ").append(order.getOrderStatus()).append("\n");

        DeliveryDTO delivery;
        try {
            delivery = deliveryService.getDeliveryByOrderId(orderId);
        } catch (ResponseStatusException e) {
            sb.append("\nNo delivery has been created for this order yet.");
            sender.sendMarkdown(ctx.chatId(), sb.toString());
            return;
        }

        sb.append("Delivery: ").append(humanStatus(delivery.getDeliveryStatus())).append("\n");

        if (isFilled(delivery.getDeliveryPerson())) {
            sb.append("Courier: ").append(delivery.getDeliveryPerson()).append("\n");
        }
        if (delivery.getEstimatedDelivery() != null) {
            sb.append("Estimated: ").append(delivery.getEstimatedDelivery().format(WHEN)).append("\n");
        }
        if (delivery.getDeliveredAt() != null) {
            sb.append("Delivered: ").append(delivery.getDeliveredAt().format(WHEN)).append("\n");
        }
        // Safe to show now that we know the chat owns this order: the code is meant
        // for the customer to read out to the driver on arrival.
        if (delivery.getDeliveryStatus() == DeliveryStatus.Shipped
                && isFilled(delivery.getDeliveryCode())) {
            sb.append("\nGive the driver this code: *").append(delivery.getDeliveryCode()).append("*");
        }

        sender.sendMarkdown(ctx.chatId(), sb.toString());
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
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
            case Delivered -> "Delivered — please confirm in the app";
            case Completed -> "Completed";
            case Disputed  -> "A problem was reported, we are looking into it";
        };
    }
}
