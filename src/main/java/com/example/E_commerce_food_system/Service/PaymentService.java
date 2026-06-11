package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.PaymentDTO;

import java.util.List;

public interface PaymentService {

    List<PaymentDTO> getAllPayments();

    PaymentDTO getPaymentById(Integer id);

    List<PaymentDTO> getPaymentsByOrderId(Integer orderId);

    PaymentDTO createPayment(PaymentDTO paymentDTO);

    PaymentDTO updatePayment(Integer id, PaymentDTO paymentDTO);

    void deletePayment(Integer id);
}