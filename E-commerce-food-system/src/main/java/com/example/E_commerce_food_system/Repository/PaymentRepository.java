package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByOrder_OrderId(Integer orderId);
}