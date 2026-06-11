package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.OrderDTO;
import java.util.List;

public interface OrderService {

    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Integer id);

    List<OrderDTO> getOrdersByCustomerId(Integer customerId);

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO updateOrder(Integer id, OrderDTO orderDTO);

    void deleteOrder(Integer id);
}