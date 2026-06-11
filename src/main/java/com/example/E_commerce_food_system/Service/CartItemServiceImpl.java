package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CartItemDTO;
import com.example.E_commerce_food_system.Entity.Cart;
import com.example.E_commerce_food_system.Entity.CartItem;
import com.example.E_commerce_food_system.Entity.Product;
import com.example.E_commerce_food_system.Repository.CartItemRepository;
import com.example.E_commerce_food_system.Repository.CartRepository;
import com.example.E_commerce_food_system.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartItemServiceImpl(
            CartItemRepository cartItemRepository,
            CartRepository cartRepository,
            ProductRepository productRepository) {

        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartItem> getAll() {
        return cartItemRepository.findAll();
    }

    @Override
    public CartItem getById(Integer id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart Item not found"));
    }

    @Override
    public CartItem create(CartItemDTO dto) {

        Cart cart = cartRepository.findById(dto.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(dto.getQuantity());

        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem update(Integer id, CartItemDTO dto) {

        CartItem cartItem = getById(id);

        Cart cart = cartRepository.findById(dto.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(dto.getQuantity());

        return cartItemRepository.save(cartItem);
    }

    @Override
    public void delete(Integer id) {
        cartItemRepository.deleteById(id);
    }
}