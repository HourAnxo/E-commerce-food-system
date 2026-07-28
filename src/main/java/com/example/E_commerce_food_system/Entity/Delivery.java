package com.example.E_commerce_food_system.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    // ===== CHANGED: a delivery sitting in the pool has no driver yet (V12) =====
    @Column(name = "delivery_person")
    private String deliveryPerson;

    @Column(name = "delivery_phone")
    private String deliveryPhone;
    // ===============

    @Column(name = "delivery_address", nullable = false, columnDefinition = "text")
    private String deliveryAddress;

    // ===== NEW =====
    @Column(name = "delivery_code", length = 6)
    private String deliveryCode;
    // ===============

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.Preparing;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    // ===== NEW =====
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    // ===============

    // ===== NEW: assignment flow =====
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /** Single-use token for the driver's offer link. Cleared on accept/decline. */
    @Column(name = "accept_token", length = 64, unique = true)
    private String acceptToken;
    // ===============

    public enum DeliveryStatus {
        Preparing,
        // ===== NEW =====
        Assigned,
        // ===============
        Shipped,
        Delivered,
        // ===== NEW =====
        Completed,
        Disputed
        // ===============
    }

    // Getters and Setters
    public Integer getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Integer deliveryId) { this.deliveryId = deliveryId; }

    public Orders getOrder() { return order; }
    public void setOrder(Orders order) { this.order = order; }

    public String getDeliveryPerson() { return deliveryPerson; }
    public void setDeliveryPerson(String deliveryPerson) { this.deliveryPerson = deliveryPerson; }

    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    // ===== NEW =====
    public String getDeliveryCode() { return deliveryCode; }
    public void setDeliveryCode(String deliveryCode) { this.deliveryCode = deliveryCode; }
    // ===============

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    // ===== NEW =====
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    // ===============

    // ===== NEW: assignment flow =====
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getAcceptToken() { return acceptToken; }
    public void setAcceptToken(String acceptToken) { this.acceptToken = acceptToken; }
    // ===============
}
