package com.example.E_commerce_food_system.DTO;

import lombok.Data;

@Data
public class CartItemDTO {

    private Integer cartId;
    private Integer productId;
    private Integer quantity;
}
