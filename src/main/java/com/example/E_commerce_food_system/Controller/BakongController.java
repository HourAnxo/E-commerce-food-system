package com.example.E_commerce_food_system.Controller;

import com.example.E_commerce_food_system.DTO.BakongQrResponse;
import com.example.E_commerce_food_system.DTO.BakongStatusResponse;
import com.example.E_commerce_food_system.Service.BakongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/payment/bakong")
public class BakongController {

    private final BakongService bakongService;

    public BakongController(BakongService bakongService) {
        this.bakongService = bakongService;
    }

    // Generate a KHQR for an order (also records a Pending Bakong payment).
    @PostMapping("/qr/{orderId}")
    public ResponseEntity<BakongQrResponse> generateQr(@PathVariable Integer orderId) {
        return ResponseEntity.ok(bakongService.generateQr(orderId));
    }

    // Poll whether the QR has been paid; marks the payment Paid when it has.
    @GetMapping("/status")
    public ResponseEntity<BakongStatusResponse> checkStatus(@RequestParam String md5,
                                                            @RequestParam Integer orderId) {
        return ResponseEntity.ok(bakongService.checkStatus(md5, orderId));
    }
}
