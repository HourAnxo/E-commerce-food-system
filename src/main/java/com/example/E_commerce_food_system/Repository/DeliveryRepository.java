package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    Optional<Delivery> findByOrder_OrderId(Integer orderId);

    List<Delivery> findByDeliveryStatusAndDeliveredAtBefore(
            Delivery.DeliveryStatus status, LocalDateTime before);

    // ===== NEW: driver opens the offer link instead of logging in =====
    Optional<Delivery> findByAcceptToken(String acceptToken);
    // ===============
}