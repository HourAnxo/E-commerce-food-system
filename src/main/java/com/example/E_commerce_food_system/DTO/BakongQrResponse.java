package com.example.E_commerce_food_system.DTO;

import java.math.BigDecimal;

/**
 * Returned when a KHQR is generated for an order.
 * The frontend renders {@code qr} as a QR code and polls the status endpoint with {@code md5}.
 */
public record BakongQrResponse(String qr, String md5, BigDecimal amount, Integer orderId) {
}
