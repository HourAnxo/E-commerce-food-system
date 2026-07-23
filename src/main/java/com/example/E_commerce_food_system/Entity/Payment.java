package com.example.E_commerce_food_system.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false,
            columnDefinition = "ENUM('Cash','Credit Card','ABA','ACELEDA','Wing','Bakong')")
    private PaymentMethod paymentMethod;

    // ===== NEW =====
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;
    // ===============

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // ===== NEW =====
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    // ===============

    public enum PaymentMethod {
        Cash,

        @JsonProperty("Credit Card")
        Credit_Card,

        ABA,
        ACELEDA,
        Wing,
        Bakong
    }

    public enum PaymentStatus {
        Pending,
        Paid,
        Failed
    }

    // Getters and Setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    public Orders getOrder() { return order; }
    public void setOrder(Orders order) { this.order = order; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    // ===== NEW =====
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // no setter for updatedAt — MySQL fills it automatically on update
    // ===============

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}