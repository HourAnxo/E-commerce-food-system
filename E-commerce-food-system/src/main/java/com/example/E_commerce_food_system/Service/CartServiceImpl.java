package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.CartDTO;
import com.example.E_commerce_food_system.Entity.Cart;
import com.example.E_commerce_food_system.Entity.Customer;
import com.example.E_commerce_food_system.Repository.CartRepository;
import com.example.E_commerce_food_system.Repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CustomerRepository customerRepository) {

        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Cart> getAll() {
        return cartRepository.findAll();
    }

    @Override
    public Cart getById(Integer id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @Override
    public Cart create(CartDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Cart cart = new Cart();
        cart.setCustomer(customer);

        return cartRepository.save(cart);
    }

    @Override
    public void delete(Integer id) {
        cartRepository.deleteById(id);
    }
}