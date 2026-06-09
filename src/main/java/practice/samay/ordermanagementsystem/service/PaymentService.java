package practice.samay.ordermanagementsystem.service;

import practice.samay.ordermanagementsystem.dto.request.PaymentRequest;
import practice.samay.ordermanagementsystem.dto.response.PaymentResponse;

import java.util.List;


public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    PaymentResponse getPaymentByReference(String reference);

    List<PaymentResponse> getPaymentsByOrderId(Long orderId);

    PaymentResponse updatePaymentStatus(Long id, String status);
}
