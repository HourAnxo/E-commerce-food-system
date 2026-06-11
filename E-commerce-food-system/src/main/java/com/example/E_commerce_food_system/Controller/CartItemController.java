package com.example.E_commerce_food_system.Controller;

import com.example.E_commerce_food_system.DTO.CartItemDTO;
import com.example.E_commerce_food_system.Entity.CartItem;
import com.example.E_commerce_food_system.Service.CartItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService service;

    public CartItemController(CartItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<CartItem> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CartItem getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public CartItem create(@RequestBody CartItemDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public CartItem update(
            @PathVariable Integer id,
            @RequestBody CartItemDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}