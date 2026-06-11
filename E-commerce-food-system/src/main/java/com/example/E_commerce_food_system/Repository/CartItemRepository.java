package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
}
