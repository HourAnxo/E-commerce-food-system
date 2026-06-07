package com.example.E_commerce_food_system.Repository;

import com.example.E_commerce_food_system.Entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByCustomer_CustomerId(Integer customerId);
}