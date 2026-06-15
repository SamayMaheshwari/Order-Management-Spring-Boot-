package practice.samay.ordermanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.cache.OrderCacheService;
import practice.samay.ordermanagementsystem.cache.PaymentCacheService;
import practice.samay.ordermanagementsystem.dao.OrderDaoImpl;
import practice.samay.ordermanagementsystem.dao.PaymentDaoImpl;
import practice.samay.ordermanagementsystem.dto.request.PaymentRequest;
import practice.samay.ordermanagementsystem.dto.response.PaymentResponse;
import practice.samay.ordermanagementsystem.dto.response.OrderResponse;
import practice.samay.ordermanagementsystem.enums.OrderStatus;
import practice.samay.ordermanagementsystem.enums.PaymentMethod;
import practice.samay.ordermanagementsystem.enums.PaymentStatus;
import practice.samay.ordermanagementsystem.exception.BusinessException;
import practice.samay.ordermanagementsystem.exception.ResourceNotFoundException;
import practice.samay.ordermanagementsystem.model.Order;
import practice.samay.ordermanagementsystem.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentDaoImpl paymentDao;
    private final OrderDaoImpl orderDao;
    private final PaymentCacheService paymentCacheService;
    private final OrderCacheService orderCacheService;
    private final HistoryEventPublisher historyEventPublisher;


    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment request for order id: {} | method: {}", request.getOrderId(), request.getPaymentMethod());

        Order order = orderDao.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        // Business rules
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot process payment for a cancelled order.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Order is already delivered; no payment needed.");
        }
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new BusinessException("Order has already been paid. Current status: " + order.getStatus());
        }

        // Validate UPI details if PaymentMethod is UPI
        if (request.getPaymentMethod() == PaymentMethod.UPI) {
            if (request.getUpiId() == null || request.getUpiId().trim().isEmpty()) {
                throw new BusinessException("UPI ID is required when payment method is UPI.");
            }
            if (!request.getUpiId().contains("@")) {
                throw new BusinessException("Invalid UPI ID format. Must contain '@' (e.g. samay@upi).");
            }
        }

        // Initialize transaction details
        String paymentReference = generatePaymentReference();
        String transactionId = request.getTransactionId() != null ? request.getTransactionId() : "TXN-" + System.currentTimeMillis();

        Payment payment = Payment.builder()
                .paymentReference(paymentReference)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .transactionId(transactionId)
                .remarks(request.getRemarks() != null ? request.getRemarks() : "Payment processed via Gateway")
                .build();

        Payment savedPayment = paymentDao.save(payment);

        // ─── Dummy Payment Gateway Simulation ───
        log.info("[PAYMENT GATEWAY] Proceeding to payment for Order: {} (Total: ₹{})", order.getOrderNumber(), request.getAmount());
        
        if (request.getPaymentMethod() == PaymentMethod.UPI) {
            log.info("[PAYMENT GATEWAY] UPI ID submitted: {}", request.getUpiId());
            log.info("[PAYMENT GATEWAY] Sending collect request to UPI App. Please open your UPI app and authorize...");
            try {
                // Wait for 3 seconds to simulate user opening app, reviewing request and entering UPI PIN
                log.info("[PAYMENT GATEWAY] [Waiting for user response - 3 seconds]...");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[PAYMENT GATEWAY] Payment verification interrupted", e);
                throw new BusinessException("Payment verification was interrupted.");
            }
            log.info("[PAYMENT GATEWAY] Authorization Success! PIN verified.");
        } else {
            log.info("[PAYMENT GATEWAY] Redirecting to Bank authentication...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("[PAYMENT GATEWAY] Authorization Success!");
        }

        // Mark payment as COMPLETED and order as CONFIRMED
        savedPayment.setStatus(PaymentStatus.COMPLETED);
        savedPayment.setPaidAt(LocalDateTime.now());
        Payment completedPayment = paymentDao.update(savedPayment);

        order.setStatus(OrderStatus.CONFIRMED);
        Order updatedOrder = orderDao.update(order);

        PaymentResponse paymentResponse = toResponse(completedPayment, updatedOrder.getOrderNumber());
        paymentCacheService.cacheSnapshot(paymentResponse);
        OrderResponse orderResponse = toOrderResponse(updatedOrder);
        orderCacheService.cacheSnapshot(orderResponse);
        orderCacheService.evictListCaches();
        historyEventPublisher.publish("PAYMENT", completedPayment.getId(), "CREATE", paymentResponse);
        historyEventPublisher.publish("ORDER", updatedOrder.getId(), "UPDATE", orderResponse);

        log.info("[PAYMENT GATEWAY] Payment {} completed successfully. Order {} status is now CONFIRMED.", 
                paymentReference, order.getOrderNumber());
        return paymentResponse;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.debug("Fetching payment by id: {}", id);
        PaymentResponse cachedPayment = paymentCacheService.getById(id).orElse(null);
        if (cachedPayment != null) {
            return cachedPayment;
        }
        Payment payment = findPaymentOrThrow(id);
        String orderNumber = resolveOrderNumber(payment.getOrderId());
        PaymentResponse paymentResponse = toResponse(payment, orderNumber);
        paymentCacheService.cacheSnapshot(paymentResponse);
        return paymentResponse;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String reference) {
        log.debug("Fetching payment by reference: {}", reference);
        PaymentResponse cachedPayment = paymentCacheService.getByReference(reference).orElse(null);
        if (cachedPayment != null) {
            return cachedPayment;
        }
        Payment payment = paymentDao.findByPaymentReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));
        PaymentResponse paymentResponse = toResponse(payment, resolveOrderNumber(payment.getOrderId()));
        paymentCacheService.cacheSnapshot(paymentResponse);
        return paymentResponse;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        log.debug("Fetching payments for order id: {}", orderId);
        List<PaymentResponse> cachedPayments = paymentCacheService.getByOrderId(orderId).orElse(null);
        if (cachedPayments != null) {
            return cachedPayments;
        }
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        List<PaymentResponse> payments = paymentDao.findByOrderId(orderId).stream()
                .map(p -> toResponse(p, order.getOrderNumber()))
                .collect(Collectors.toList());
        paymentCacheService.cacheByOrderId(orderId, payments);
        return payments;
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, String status) {
        log.info("Updating payment {} status to: {}", id, status);
        Payment payment = findPaymentOrThrow(id);
        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
            payment.setStatus(newStatus);
            if (newStatus == PaymentStatus.COMPLETED && payment.getPaidAt() == null) {
                payment.setPaidAt(LocalDateTime.now());
            }
            Payment updated = paymentDao.update(payment);
            PaymentResponse paymentResponse = toResponse(updated, resolveOrderNumber(updated.getOrderId()));
            paymentCacheService.cacheSnapshot(paymentResponse);
            historyEventPublisher.publish("PAYMENT", updated.getId(), "UPDATE", paymentResponse);
            log.info("Payment {} status updated to: {}", id, newStatus);
            return paymentResponse;
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid payment status: " + status +
                    ". Valid values: PENDING, COMPLETED, FAILED, REFUNDED");
        }
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private Payment findPaymentOrThrow(Long id) {
        return paymentDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private String resolveOrderNumber(Long orderId) {
        return orderDao.findById(orderId).map(Order::getOrderNumber).orElse(null);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .build();
    }

//    private OrderResponse toOrderResponse(Order order) {
//        return OrderResponse.builder()
//                .id(order.getId())
//                .orderNumber(order.getOrderNumber())
//                .customerName(order.getCustomerName())
//                .customerEmail(order.getCustomerEmail())
//                .customerPhone(order.getCustomerPhone())
//                .shippingAddress(order.getShippingAddress())
//                .productCode(order.getProductCode())
//                .productName(order.getProductName())
//                .quantity(order.getQuantity())
//                .unitPrice(order.getUnitPrice())
//                .totalAmount(order.getTotalAmount())
//                .status(order.getStatus().name())
//                .notes(order.getNotes())
//                .createdAt(order.getCreatedAt())
//                .updatedAt(order.getUpdatedAt())
//                .build();
//    }

    private String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    private PaymentResponse toResponse(Payment payment, String orderNumber) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .orderId(payment.getOrderId())
                .orderNumber(orderNumber)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .remarks(payment.getRemarks())
                .build();
    }

//    private PaymentResponse toResponse(Payment payment, String orderNumber) {
//        return PaymentResponse.builder()
//                .id(payment.getId())
//                .paymentReference(payment.getPaymentReference())
//                .orderId(payment.getOrderId())
//                .orderNumber(orderNumber)
//                .amount(payment.getAmount())
//                .paymentMethod(payment.getPaymentMethod().name())
//                .status(payment.getStatus().name())
//                .transactionId(payment.getTransactionId())
//                .remarks(payment.getRemarks())
//                .paidAt(payment.getPaidAt())
//                .createdAt(payment.getCreatedAt())
//                .updatedAt(payment.getUpdatedAt())
//                .build();
//    }
}
