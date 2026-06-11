package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory_CategoryId(Integer categoryId);
}