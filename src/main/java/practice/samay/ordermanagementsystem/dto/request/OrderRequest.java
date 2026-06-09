package practice.samay.ordermanagementsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new order.
 * All fields are validated before hitting the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OrderRequest", description = "Payload for creating a new order")
public class OrderRequest {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    @Schema(description = "Full name of the customer", example = "Samay Sharma")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Customer email address", example = "samay@example.com")
    private String customerEmail;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(description = "Customer contact number", example = "+919876543210")
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    @Schema(description = "Full delivery address", example = "123 MG Road, Pune, Maharashtra - 411001")
    private String shippingAddress;

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    @Schema(description = "Unique product code from inventory", example = "PROD-001")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Schema(description = "Name of the product", example = "Wireless Bluetooth Headphones")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity must not exceed 1000")
    @Schema(description = "Number of units ordered", example = "2")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be a positive value")
    @Digits(integer = 10, fraction = 2, message = "Unit price must have at most 10 digits and 2 decimal places")
    @Schema(description = "Price per unit in INR", example = "2999.99")
    private BigDecimal unitPrice;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Optional order notes or special instructions", example = "Please handle with care")
    private String notes;
}
