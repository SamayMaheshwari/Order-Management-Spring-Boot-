package practice.samay.ordermanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import practice.samay.ordermanagementsystem.dto.request.PaymentRequest;
import practice.samay.ordermanagementsystem.dto.response.ApiResponse;
import practice.samay.ordermanagementsystem.dto.response.PaymentResponse;
import practice.samay.ordermanagementsystem.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Payments", description = "Payment Management APIs – Process and track order payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    // CREATE

    @PostMapping
    @Operation(
        summary = "Process a payment",
        description = "Processes payment for an order. On success, order status is automatically " +
                      "updated to CONFIRMED. Supports multiple payment methods."
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/v1/payments – order id: {} | method: {}", request.getOrderId(), request.getPaymentMethod());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", response));
    }

    //  READ

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by its database ID.")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "Payment database ID", example = "1")
            @PathVariable Long id) {
        log.debug("GET /api/v1/payments/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", paymentService.getPaymentById(id)));
    }

    @GetMapping("/reference/{reference}")
    @Operation(
        summary = "Get payment by reference",
        description = "Retrieves payment by its auto-generated reference number (e.g. PAY-...)."
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(
            @Parameter(description = "Payment reference number", example = "PAY-1718000000000-7823")
            @PathVariable String reference) {
        log.debug("GET /api/v1/payments/reference/{}", reference);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", paymentService.getPaymentByReference(reference)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Get payments for an order",
        description = "Retrieves all payment records associated with a specific order."
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrderId(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long orderId) {
        log.debug("GET /api/v1/payments/order/{}", orderId);
        List<PaymentResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(
                "Payments retrieved for order id: " + orderId, payments));
    }

    //  UPDATE

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update payment status",
        description = "Manually updates a payment status. " +
                      "Valid values: PENDING, COMPLETED, FAILED, REFUNDED"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @Parameter(description = "Payment ID", example = "1") @PathVariable Long id,
            @Parameter(description = "New payment status", example = "REFUNDED")
            @RequestParam String status) {
        log.info("PUT /api/v1/payments/{}/status – new status: {}", id, status);
        return ResponseEntity.ok(ApiResponse.success(
                "Payment status updated successfully", paymentService.updatePaymentStatus(id, status)));
    }
}
