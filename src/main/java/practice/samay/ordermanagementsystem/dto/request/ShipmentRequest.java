package practice.samay.ordermanagementsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ShipmentRequest", description = "Payload for creating a new shipment")
public class ShipmentRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID of the confirmed order to ship", example = "1")
    private Long orderId;

    @NotBlank(message = "Carrier name is required")
    @Size(max = 100, message = "Carrier name must not exceed 100 characters")
    @Schema(description = "Shipping carrier / logistics provider", example = "BlueDart")
    private String carrier;

    @Schema(description = "Delivery address (defaults to order's shipping address if not provided)",
            example = "123 MG Road, Pune, Maharashtra - 411001")
    private String shippingAddress;

    @Positive(message = "Weight must be a positive value")
    @Schema(description = "Package weight in kilograms", example = "1.5")
    private Double weight;

    @Future(message = "Estimated delivery date must be in the future")
    @Schema(description = "Expected delivery date (ISO format yyyy-MM-dd)", example = "2026-06-20")
    private LocalDate estimatedDelivery;
}
