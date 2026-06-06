package com.example.E_commerce_food_system.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name="products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productid;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name="product_name")
    private String productname;

    private String description;

    private BigDecimal price;

    @Column(name="stock_quantity")
    private Integer stockquantity;

    @Column(name="image_url")
    private String imageurl;
}
