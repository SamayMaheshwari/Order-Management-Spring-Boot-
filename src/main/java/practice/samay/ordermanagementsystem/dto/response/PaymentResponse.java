package practice.samay.ordermanagementsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Payment data returned to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PaymentResponse", description = "Payment details returned in API responses")
public class PaymentResponse {

    @Schema(description = "Unique payment ID", example = "1")
    private Long id;

    @Schema(description = "Auto-generated payment reference", example = "PAY-1718000000000-7823")
    private String paymentReference;

    @Schema(description = "Associated order ID", example = "1")
    private Long orderId;

    @Schema(description = "Associated order number", example = "ORD-1718000000000-4231")
    private String orderNumber;

    @Schema(description = "Payment amount in INR", example = "5999.98")
    private BigDecimal amount;

    @Schema(description = "Payment method used", example = "UPI")
    private String paymentMethod;

    @Schema(description = "Current payment status", example = "COMPLETED")
    private String status;

    @Schema(description = "External transaction ID from payment gateway", example = "TXN1234567890")
    private String transactionId;

    @Schema(description = "Payment remarks")
    private String remarks;

    @Schema(description = "Timestamp when payment was completed")
    private LocalDateTime paidAt;

    @Schema(description = "Payment creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
