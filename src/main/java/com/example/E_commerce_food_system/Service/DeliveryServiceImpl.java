package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import com.example.E_commerce_food_system.Entity.Delivery;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Repository.DeliveryRepository;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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

        // ===== NEW: if created directly as Shipped, it still needs a code =====
        if (delivery.getDeliveryStatus() == Delivery.DeliveryStatus.Shipped) {
            delivery.setDeliveryCode(generateCode());
        }
        // ===============

        return toDTO(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryDTO updateDelivery(Integer id, DeliveryDTO dto) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // MUST be here — before setDeliveryStatus below
        if (dto.getDeliveryStatus() == Delivery.DeliveryStatus.Shipped
                && delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Shipped) {
            delivery.setDeliveryCode(generateCode());
        }

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

    // ===== NEW: driver enters the customer's code -> Delivered =====
    @Override
    @Transactional
    public DeliveryDTO completeDelivery(Integer deliveryId, String code) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found with id: " + deliveryId));

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Shipped) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery is not in Shipped status");
        }
        if (delivery.getDeliveryCode() == null
                || !delivery.getDeliveryCode().equals(code)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid delivery code");
        }

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Delivered);
        delivery.setDeliveredAt(LocalDateTime.now());
        return toDTO(deliveryRepository.save(delivery));
    }

    // ===== NEW: customer confirms they received the product -> Completed =====
    @Override
    @Transactional
    public DeliveryDTO confirmDelivery(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found with id: " + deliveryId));

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Delivered) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery is not in Delivered status");
        }

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Completed);
        delivery.setConfirmedAt(LocalDateTime.now());
        return toDTO(deliveryRepository.save(delivery));
    }

    // ===== NEW: customer reports a problem -> Disputed =====
    @Override
    @Transactional
    public DeliveryDTO reportProblem(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found with id: " + deliveryId));

        if (delivery.getDeliveryStatus() != Delivery.DeliveryStatus.Delivered) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery is not in Delivered status");
        }

        delivery.setDeliveryStatus(Delivery.DeliveryStatus.Disputed);
        return toDTO(deliveryRepository.save(delivery));
    }

    // ===== NEW: auto-confirm Delivered deliveries older than 3 days =====
    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void autoConfirmDeliveries() {
        List<Delivery> stale = deliveryRepository
                .findByDeliveryStatusAndDeliveredAtBefore(
                        Delivery.DeliveryStatus.Delivered,
                        LocalDateTime.now().minusDays(3));
        for (Delivery d : stale) {
            d.setDeliveryStatus(Delivery.DeliveryStatus.Completed);
            d.setConfirmedAt(LocalDateTime.now());
        }
        deliveryRepository.saveAll(stale);
    }

    // ===== NEW: 6-digit random code =====
    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
    // ===============

    private DeliveryDTO toDTO(Delivery delivery) {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setOrderId(delivery.getOrder().getOrderId());
        dto.setDeliveryPerson(delivery.getDeliveryPerson());
        dto.setDeliveryPhone(delivery.getDeliveryPhone());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setDeliveryStatus(delivery.getDeliveryStatus());
        dto.setEstimatedDelivery(delivery.getEstimatedDelivery());
        // ===== NEW =====
        dto.setDeliveryCode(delivery.getDeliveryCode());
        dto.setDeliveredAt(delivery.getDeliveredAt());
        dto.setConfirmedAt(delivery.getConfirmedAt());
        // ===============
        return dto;
    }
}