package practice.samay.ordermanagementsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ShipmentResponse", description = "Shipment details returned in API responses")
public class ShipmentResponse {

    @Schema(description = "Unique shipment ID", example = "1")
    private Long id;

    @Schema(description = "Associated order ID", example = "1")
    private Long orderId;

    @Schema(description = "Auto-generated tracking number", example = "TRK-1718000000000-5512")
    private String trackingNumber;

    @Schema(description = "Shipping carrier name", example = "BlueDart")
    private String carrier;

    @Schema(description = "Current shipment status", example = "IN_TRANSIT")
    private String status;

    @Schema(description = "Delivery address")
    private String shippingAddress;

    @Schema(description = "Package weight in kg", example = "1.5")
    private Double weight;

    @Schema(description = "Expected delivery date", example = "2026-06-20")
    private LocalDate estimatedDelivery;


}
