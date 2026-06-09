package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {
}