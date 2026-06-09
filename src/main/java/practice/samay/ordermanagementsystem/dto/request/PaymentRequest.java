package practice.samay.ordermanagementsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import practice.samay.ordermanagementsystem.enums.PaymentMethod;

import java.math.BigDecimal;

/**
 * Request DTO for processing a payment against an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PaymentRequest", description = "Payload for processing a payment")
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID of the order to pay for", example = "1")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Payment amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 digits and 2 decimal places")
    @Schema(description = "Payment amount in INR", example = "5999.98")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Method used for payment", example = "UPI",
            allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING", "CASH_ON_DELIVERY", "WALLET"})
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Schema(description = "External transaction / reference ID from payment gateway", example = "TXN1234567890")
    private String transactionId;

    @Schema(description = "UPI ID required if payment method is UPI", example = "samay@upi")
    private String upiId;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    @Schema(description = "Optional payment remarks or notes", example = "Paid via Google Pay")
    private String remarks;
}
