package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CartItemDTO;
import com.example.E_commerce_food_system.Entity.Cart;
import com.example.E_commerce_food_system.Entity.CartItem;

import java.util.List;

public interface CartItemService {

    List<CartItem> getAll();
    CartItem getById(Integer id);
    CartItem create(CartItemDTO dto);
    CartItem update(Integer id, CartItemDTO dto);

    void delete(Integer id);
}
