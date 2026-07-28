package com.example.E_commerce_food_system.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One row per driver who turned down a delivery.
 * The unique key on (delivery_id, delivery_person) is what stops the same
 * driver being offered the same delivery twice.
 */
@Entity
@Table(name = "delivery_decline")
public class DeliveryDecline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "delivery_id", nullable = false)
    private Integer deliveryId;

    @Column(name = "delivery_person", nullable = false, length = 100)
    private String deliveryPerson;

    @Column(name = "declined_at", nullable = false)
    private LocalDateTime declinedAt;

    protected DeliveryDecline() {
    }

    public DeliveryDecline(Integer deliveryId, String deliveryPerson, LocalDateTime declinedAt) {
        this.deliveryId = deliveryId;
        this.deliveryPerson = deliveryPerson;
        this.declinedAt = declinedAt;
    }

    public Integer getId() { return id; }

    public Integer getDeliveryId() { return deliveryId; }

    public String getDeliveryPerson() { return deliveryPerson; }

    public LocalDateTime getDeclinedAt() { return declinedAt; }
}
