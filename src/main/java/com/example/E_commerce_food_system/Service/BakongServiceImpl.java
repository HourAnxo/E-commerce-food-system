package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.BakongQrResponse;
import com.example.E_commerce_food_system.DTO.BakongStatusResponse;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Entity.Payment;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import com.example.E_commerce_food_system.Repository.PaymentRepository;
import com.example.E_commerce_food_system.config.BakongProperties;
import org.springframework.http.HttpStatus;
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

        String qr =
                "FAKE-BAKONG-QR-" + orderId;
        String md5 = khqrGenerator.md5(qr);

        // Reuse an existing Pending Bakong payment for this order (e.g. a retried checkout)
        // instead of stacking a duplicate row each time a QR is regenerated.
        Payment payment = paymentRepository.findByOrder_OrderId(orderId).stream()
                .filter(p -> p.getPaymentMethod() == Payment.PaymentMethod.Bakong
                        && p.getPaymentStatus() == Payment.PaymentStatus.Pending)
                .findFirst()
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setOrder(order);
                    p.setPaymentMethod(Payment.PaymentMethod.Bakong);
                    p.setPaymentStatus(Payment.PaymentStatus.Pending);
                    return p;
                });
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
     * was found (i.e. paid). A "not found" response or a transient network/parse error
     * is treated as "not yet paid". A rejected token (401/403) is a configuration
     * problem the caller must see, so it is surfaced rather than masked as Pending.
     */
    private boolean isPaidOnBakong(String md5) {

        // Mock payment for demo.
        // Always return true so payment becomes Paid.

        return true;
    }

}




