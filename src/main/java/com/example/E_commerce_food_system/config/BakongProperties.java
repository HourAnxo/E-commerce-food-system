package com.example.E_commerce_food_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bakong KHQR settings, bound from the {@code bakong.*} properties.
 * Real values come from env vars (see application.properties) — never hardcode the token.
 */
@Component
@ConfigurationProperties(prefix = "bakong")
@Getter
@Setter
public class BakongProperties {
    /** Base URL of the Bakong Open API, e.g. https://api-bakong.nbc.gov.kh */
    private String apiUrl;
    /** Bakong account identifier that receives the money, e.g. name@bank */
    private String accountId;
    /** Bearer token issued at https://api-bakong.nbc.gov.kh */
    private String apiToken;
    /** Merchant name shown on the QR (max 25 chars). */
    private String merchantName;
    /** Merchant city shown on the QR (max 15 chars). */
    private String merchantCity;
    /** "USD" or "KHR" — controls the EMV currency code and amount formatting. */
    private String currency;
}
