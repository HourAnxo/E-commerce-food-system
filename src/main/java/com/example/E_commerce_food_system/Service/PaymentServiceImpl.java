package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.PaymentDTO;
import com.example.E_commerce_food_system.Entity.Orders;
import com.example.E_commerce_food_system.Entity.Payment;
import com.example.E_commerce_food_system.Repository.OrderRepository;
import com.example.E_commerce_food_system.Repository.PaymentRepository;
import com.example.E_commerce_food_system.Service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // Map Entity -> DTO
    private PaymentDTO toDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrder().getOrderId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentDate(payment.getPaymentDate());
        return dto;
    }

    // Map DTO -> Entity  ← THIS IS WHERE YOUR BUG WAS
    private Payment toEntity(PaymentDTO dto) {
        // ✅ Fetch the real Orders object from DB
        Orders order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + dto.getOrderId()));

        Payment payment = new Payment();
        payment.setOrder(order);  // ✅ Set real Orders object, not null
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(dto.getPaymentStatus() != null
                ? dto.getPaymentStatus()
                : Payment.PaymentStatus.Pending);
        payment.setPaymentDate(LocalDateTime.now());
        return payment;
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found with id: " + id));
        return toDTO(payment);
    }

    @Override
    public List<PaymentDTO> getPaymentsByOrderId(Integer orderId) {
        return paymentRepository.findByOrder_OrderId(orderId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = toEntity(paymentDTO);
        return toDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentDTO updatePayment(Integer id, PaymentDTO paymentDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found with id: " + id));

        Orders order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + paymentDTO.getOrderId()));

        payment.setOrder(order);
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setPaymentStatus(paymentDTO.getPaymentStatus());
        payment.setPaymentDate(paymentDTO.getPaymentDate());

        return toDTO(paymentRepository.save(payment));
    }

    @Override
    public void deletePayment(Integer id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
}