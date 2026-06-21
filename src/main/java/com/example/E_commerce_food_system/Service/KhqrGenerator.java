package com.example.E_commerce_food_system.Service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Builds a Bakong KHQR payload string following the EMVCo QR spec (Cambodia / NBC).
 * Generation is done locally; the Bakong Open API is only used afterwards to check
 * whether the QR has been paid (via the MD5 of this exact string).
 */
@Component
public class KhqrGenerator {

    /**
     * @param accountId    Bakong account identifier (name@bank) that receives payment
     * @param merchantName name shown to the payer (<= 25 chars)
     * @param merchantCity city shown to the payer (<= 15 chars)
     * @param amount       transaction amount
     * @param currency     "USD" or "KHR"
     * @param billNumber   reference shown to payer (e.g. the order id)
     */
    public String generate(String accountId, String merchantName, String merchantCity,
                           BigDecimal amount, String currency, String billNumber) {
        boolean isKhr = "KHR".equalsIgnoreCase(currency);
        String currencyCode = isKhr ? "116" : "840";
        // KHR has no minor unit; USD uses 2 decimals.
        String amountStr = isKhr
                ? String.valueOf(amount.setScale(0, java.math.RoundingMode.HALF_UP).intValueExact())
                : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();

        StringBuilder sb = new StringBuilder();
        sb.append(tlv("00", "01"));                       // Payload format indicator
        sb.append(tlv("01", "12"));                       // Point of initiation: dynamic (one-time)
        sb.append(tlv("29", tlv("00", accountId)));       // Individual account info -> Bakong account id
        sb.append(tlv("52", "5999"));                     // Merchant category code (misc)
        sb.append(tlv("53", currencyCode));               // Transaction currency
        sb.append(tlv("54", amountStr));                  // Transaction amount
        sb.append(tlv("58", "KH"));                       // Country code
        sb.append(tlv("59", clip(merchantName, 25)));     // Merchant name
        sb.append(tlv("60", clip(merchantCity, 15)));     // Merchant city
        sb.append(tlv("62", tlv("01", clip(billNumber, 25)))); // Additional data -> bill number

        // CRC is computed over everything including the CRC tag id + length ("6304").
        String withoutCrc = sb.append("6304").toString();
        sb.append(crc16(withoutCrc));
        return sb.toString();
    }

    /** MD5 of the QR string, lower-case hex — Bakong's transaction lookup key. */
    public String md5(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 unavailable", e); // never happens on a standard JVM
        }
    }

    /** EMV tag-length-value: 2-char tag, 2-digit length, value. */
    private String tlv(String tag, String value) {
        return tag + String.format("%02d", value.length()) + value;
    }

    private String clip(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max);
    }

    /** CRC-16/CCITT-FALSE (poly 0x1021, init 0xFFFF), as 4 upper-case hex chars. */
    private String crc16(String data) {
        int crc = 0xFFFF;
        for (byte b : data.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = ((crc & 0x8000) != 0) ? (crc << 1) ^ 0x1021 : crc << 1;
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }
}
