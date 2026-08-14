package com.example.E_commerce_food_system.Service;

/**
 * Raised when a driver accepts a delivery and it starts moving.
 *
 * Carries plain values rather than entities: listeners run after the transaction
 * has committed, where a lazy association would no longer be loadable.
 *
 * The delivery logic stays unaware of Telegram — anything that wants to react
 * (email, SMS, push) subscribes without DeliveryServiceImpl changing.
 */
public record DeliveryAcceptedEvent(
        Integer customerId,
        Integer orderId,
        String deliveryPerson,
        String deliveryCode
) {}
