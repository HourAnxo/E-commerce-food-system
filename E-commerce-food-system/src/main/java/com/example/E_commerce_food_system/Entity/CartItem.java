package com.example.E_commerce_food_system.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="cart_item")
@Data

public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "cart_item_id")
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;

    @Column(name="quantity")
    private Integer quantity;
}
