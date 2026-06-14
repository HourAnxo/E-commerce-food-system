package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import com.example.E_commerce_food_system.Entity.Delivery;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Repository.DeliveryRepository;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<DeliveryDTO> getAllDeliveries() {
        return deliveryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryDTO getDeliveryById(Integer id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        return toDTO(delivery);
    }

    @Override
    public DeliveryDTO getDeliveryByOrderId(Integer orderId) {
        Delivery delivery = deliveryRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
        return toDTO(delivery);
    }

    @Override
    public DeliveryDTO createDelivery(DeliveryDTO dto) {
        Orders order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setDeliveryPerson(dto.getDeliveryPerson());
        delivery.setDeliveryPhone(dto.getDeliveryPhone());
        delivery.setDeliveryAddress(dto.getDeliveryAddress());
        delivery.setEstimatedDelivery(dto.getEstimatedDelivery());
        if (dto.getDeliveryStatus() != null) {
            delivery.setDeliveryStatus(dto.getDeliveryStatus());
        }

        return toDTO(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryDTO updateDelivery(Integer id, DeliveryDTO dto) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setDeliveryPerson(dto.getDeliveryPerson());
        delivery.setDeliveryPhone(dto.getDeliveryPhone());
        delivery.setDeliveryAddress(dto.getDeliveryAddress());
        delivery.setEstimatedDelivery(dto.getEstimatedDelivery());
        delivery.setDeliveryStatus(dto.getDeliveryStatus());

        return toDTO(deliveryRepository.save(delivery));
    }

    @Override
    public void deleteDelivery(Integer id) {
        deliveryRepository.deleteById(id);
    }

    private DeliveryDTO toDTO(Delivery delivery) {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setOrderId(delivery.getOrder().getOrderId());
        dto.setDeliveryPerson(delivery.getDeliveryPerson());
        dto.setDeliveryPhone(delivery.getDeliveryPhone());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setDeliveryStatus(delivery.getDeliveryStatus());
        dto.setEstimatedDelivery(delivery.getEstimatedDelivery());
        return dto;
    }
}