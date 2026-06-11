package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByEmail(String email);      // ← add this
    boolean existsByEmail(String email);             // ← add this
}