package com.example.E_commerce_food_system.Controller;

import com.example.E_commerce_food_system.DTO.DeliveryDTO;
import com.example.E_commerce_food_system.Service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryDTO> getDeliveryByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@RequestBody DeliveryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.createDelivery(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryDTO> updateDelivery(@PathVariable Integer id,
                                                      @RequestBody DeliveryDTO dto) {
        return ResponseEntity.ok(deliveryService.updateDelivery(id, dto));
    }

    // ===== NEW: driver/admin enters the customer's 6-digit code -> Delivered =====
    @PostMapping("/{id}/complete")
    public ResponseEntity<DeliveryDTO> completeDelivery(@PathVariable Integer id,
                                                        @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(deliveryService.completeDelivery(id, body.get("code")));
    }

    // ===== NEW: customer confirms they received the product -> Completed =====
    @PostMapping("/{id}/confirm")
    public ResponseEntity<DeliveryDTO> confirmDelivery(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.confirmDelivery(id));
    }

    // ===== NEW: customer reports a problem -> Disputed =====
    @PostMapping("/{id}/problem")
    public ResponseEntity<DeliveryDTO> reportProblem(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.reportProblem(id));
    }
    // ===============

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Integer id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }
}