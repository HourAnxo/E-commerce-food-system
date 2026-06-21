package com.example.E_commerce_food_system.DTO;

/** Result of polling Bakong for a KHQR payment. {@code status} is "Paid" or "Pending". */
public record BakongStatusResponse(String status) {
}
