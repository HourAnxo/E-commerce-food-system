package com.example.E_commerce_food_system.Controller;

import com.example.E_commerce_food_system.DTO.CartDTO;
import com.example.E_commerce_food_system.Entity.Cart;
import com.example.E_commerce_food_system.Service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public List<Cart> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Cart getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public Cart create(@RequestBody CartDTO dto) {
        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}