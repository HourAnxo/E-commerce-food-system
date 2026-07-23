package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import java.util.List;

public interface DeliveryService {
    List<DeliveryDTO> getAllDeliveries();
    DeliveryDTO getDeliveryById(Integer id);
    DeliveryDTO getDeliveryByOrderId(Integer orderId);
    DeliveryDTO createDelivery(DeliveryDTO dto);
    DeliveryDTO updateDelivery(Integer id, DeliveryDTO dto);
    void deleteDelivery(Integer id);
    // ===== NEW =====
    DeliveryDTO completeDelivery(Integer deliveryId, String code);   // driver enters code
    DeliveryDTO confirmDelivery(Integer deliveryId);                 // customer confirms
    DeliveryDTO reportProblem(Integer deliveryId);                   // customer disputes
// ===============
}