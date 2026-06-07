package com.example.E_commerce_food_system.DTO;

import com.example.E_commerce_food_system.Entity.Orders;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {

    private Integer orderId;
    private Integer customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private Orders.OrderStatus orderStatus;

    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Orders.OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Orders.OrderStatus orderStatus) { this.orderStatus = orderStatus; }
}