package com.example.E_commerce_food_system.DTO;

import com.example.E_commerce_food_system.Entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {

    private Integer paymentId;
    private Integer orderId;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;

    // ===== NEW =====
    private BigDecimal amount;
    private String transactionRef;
    // ===============

    // Getters and Setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Payment.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Payment.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Payment.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    // ===== NEW =====
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    // ===============
}