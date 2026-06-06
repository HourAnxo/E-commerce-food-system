package com.example.E_commerce_food_system.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Integer categoryId;
    private String  productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
}
