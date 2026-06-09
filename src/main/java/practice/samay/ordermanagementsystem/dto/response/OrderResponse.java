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
 * Response DTO for Order data returned to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "OrderResponse", description = "Order details returned in API responses")
public class OrderResponse {

    @Schema(description = "Unique order ID", example = "1")
    private Long id;

    @Schema(description = "Auto-generated order number", example = "ORD-1718000000000-4231")
    private String orderNumber;

    @Schema(description = "Customer full name", example = "Samay Sharma")
    private String customerName;

    @Schema(description = "Customer email address", example = "samay@example.com")
    private String customerEmail;

    @Schema(description = "Customer phone number", example = "+919876543210")
    private String customerPhone;

    @Schema(description = "Delivery address")
    private String shippingAddress;

    @Schema(description = "Product code", example = "PROD-001")
    private String productCode;

    @Schema(description = "Product name", example = "Wireless Bluetooth Headphones")
    private String productName;

    @Schema(description = "Ordered quantity", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price in INR", example = "2999.99")
    private BigDecimal unitPrice;

    @Schema(description = "Total order amount in INR", example = "5999.98")
    private BigDecimal totalAmount;

    @Schema(description = "Current order status", example = "CONFIRMED")
    private String status;

    @Schema(description = "Order notes")
    private String notes;

    @Schema(description = "Order creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
