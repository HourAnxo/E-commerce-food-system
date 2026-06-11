package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}