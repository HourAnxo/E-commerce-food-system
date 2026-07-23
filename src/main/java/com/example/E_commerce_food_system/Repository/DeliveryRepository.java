package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    // CHANGED: List -> Optional (one delivery per order)
    Optional<Delivery> findByOrder_OrderId(Integer orderId);

    // NEW: for the auto-confirm scheduled job
    List<Delivery> findByDeliveryStatusAndDeliveredAtBefore(
            Delivery.DeliveryStatus status, LocalDateTime before);
}