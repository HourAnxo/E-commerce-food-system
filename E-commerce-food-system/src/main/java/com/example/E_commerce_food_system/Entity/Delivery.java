package com.example.E_commerce_food_system.Entity;

import jakarta.persistence.*;
import org.hibernate.query.Order;

@Entity
@Table(name= "Delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="delivery_id")
    private Integer deliveryId;

    @OneToOne
    @JoinColumn(name="order_id")
    private Order order;


}
