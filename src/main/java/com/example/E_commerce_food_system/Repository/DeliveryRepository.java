package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    Optional<Delivery> findByOrder_OrderId(Integer orderId);
}