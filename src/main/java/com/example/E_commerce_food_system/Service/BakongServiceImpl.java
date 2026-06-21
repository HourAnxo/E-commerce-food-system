package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.BakongQrResponse;
import com.example.E_commerce_food_system.DTO.BakongStatusResponse;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Entity.Payment;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import com.example.E_commerce_food_system.Repository.PaymentRepository;
import com.example.E_commerce_food_system.config.BakongProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BakongServiceImpl implements BakongService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final KhqrGenerator khqrGenerator;
    private final BakongProperties props;
    private final RestClient restClient;

    public BakongServiceImpl(OrderRepository orderRepository,
                             PaymentRepository paymentRepository,
                             KhqrGenerator khqrGenerator,
                             BakongProperties props) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.khqrGenerator = khqrGenerator;
        this.props = props;
        this.restClient = RestClient.builder().baseUrl(props.getApiUrl()).build();
    }

    @Override
    public BakongQrResponse generateQr(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        String qr = khqrGenerator.generate(
                props.getAccountId(),
                props.getMerchantName(),
                props.getMerchantCity(),
                order.getTotalAmount(),
                props.getCurrency(),
                "ORDER" + orderId);
        String md5 = khqrGenerator.md5(qr);

        // Record a Pending Bakong payment so the order has a payment row from the start.
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(Payment.PaymentMethod.Bakong);
        payment.setPaymentStatus(Payment.PaymentStatus.Pending);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return new BakongQrResponse(qr, md5, order.getTotalAmount(), orderId);
    }

    @Override
    public BakongStatusResponse checkStatus(String md5, Integer orderId) {
        if (!isPaidOnBakong(md5)) {
            return new BakongStatusResponse("Pending");
        }
        // Mark the order's Bakong payment(s) as Paid.
        List<Payment> payments = paymentRepository.findByOrder_OrderId(orderId);
        for (Payment p : payments) {
            if (p.getPaymentMethod() == Payment.PaymentMethod.Bakong
                    && p.getPaymentStatus() != Payment.PaymentStatus.Paid) {
                p.setPaymentStatus(Payment.PaymentStatus.Paid);
                p.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(p);
            }
        }
        return new BakongStatusResponse("Paid");
    }

    /**
     * Calls Bakong's check_transaction_by_md5. responseCode 0 means the transaction
     * was found (i.e. paid). Anything else — including a "not found" response or a
     * network/token error — is treated as "not yet paid".
     */
    private boolean isPaidOnBakong(String md5) {
        try {
            Map<?, ?> body = restClient.post()
                    .uri("/v1/check_transaction_by_md5")
                    .header("Authorization", "Bearer " + props.getApiToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("md5", md5))
                    .retrieve()
                    .body(Map.class);
            Object code = body == null ? null : body.get("responseCode");
            return code instanceof Number n && n.intValue() == 0;
        } catch (Exception e) {
            // Unauthorized / network issues / unexpected payloads: not paid (yet).
            return false;
        }
    }
}
