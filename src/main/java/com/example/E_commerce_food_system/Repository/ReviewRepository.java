package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}