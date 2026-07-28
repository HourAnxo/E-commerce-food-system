package com.example.E_commerce_food_system.DTO;

import com.example.E_commerce_food_system.Entity.Delivery.DeliveryStatus;
import java.time.LocalDateTime;

public class DeliveryDTO {

    private Integer deliveryId;
    private Integer orderId;
    private String deliveryPerson;
    private String deliveryPhone;
    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime estimatedDelivery;

    private String deliveryCode;   // only filled for the customer's own request
    private LocalDateTime deliveredAt;
    private LocalDateTime confirmedAt;

    // ===== NEW: assignment flow =====
    private LocalDateTime assignedAt;
    private String acceptToken;    // admin copies this into the driver's link
    // ===============

    // Getters and Setters
    public Integer getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Integer deliveryId) { this.deliveryId = deliveryId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public String getDeliveryPerson() { return deliveryPerson; }
    public void setDeliveryPerson(String deliveryPerson) { this.deliveryPerson = deliveryPerson; }

    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public String getDeliveryCode() { return deliveryCode; }
    public void setDeliveryCode(String deliveryCode) { this.deliveryCode = deliveryCode; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    // ===== NEW =====
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getAcceptToken() { return acceptToken; }
    public void setAcceptToken(String acceptToken) { this.acceptToken = acceptToken; }
    // ===============
}