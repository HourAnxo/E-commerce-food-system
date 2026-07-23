package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByOrder_OrderId(Integer orderId);

    // ===== NEW: used to block paying an order twice =====
    boolean existsByOrder_OrderIdAndPaymentStatus(Integer orderId, Payment.PaymentStatus status);

    // ===== NEW: for the Bakong flow — find the Pending payment by its KHQR md5 =====
    Optional<Payment> findByTransactionRef(String transactionRef);
}