package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CartDTO;
import com.example.E_commerce_food_system.Entity.Cart;

import java.util.List;

public interface CartService {

    List<Cart> getAll();

    Cart getById(Integer id);

    Cart create(CartDTO dto);

    void delete(Integer id);
}
