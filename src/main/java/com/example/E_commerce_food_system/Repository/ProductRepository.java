package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}