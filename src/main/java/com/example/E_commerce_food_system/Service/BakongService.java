package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.BakongQrResponse;
import com.example.E_commerce_food_system.DTO.BakongStatusResponse;

public interface BakongService {

    /** Generate a KHQR for the order's total and record a Pending Bakong payment. */
    BakongQrResponse generateQr(Integer orderId);

    /** Ask Bakong whether the QR (by md5) is paid; if so, mark the order's payment Paid. */
    BakongStatusResponse checkStatus(String md5, Integer orderId);
}
