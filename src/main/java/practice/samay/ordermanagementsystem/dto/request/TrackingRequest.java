package practice.samay.ordermanagementsystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Request DTO for adding a tracking event to a shipment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TrackingRequest", description = "Payload for adding a tracking checkpoint event")
public class TrackingRequest {

    @NotNull(message = "Shipment ID is required")
    @Schema(description = "ID of the shipment this tracking event belongs to", example = "1")
    private Long shipmentId;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Current location of the shipment", example = "Mumbai Sorting Facility")
    private String location;

    @NotNull(message = "Status is required")
    @Schema(description = "Shipment status at this tracking point", example = "IN_TRANSIT",
            allowableValues = {"PREPARING", "DISPATCHED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "RETURNED"})
    private ShipmentStatus status;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Human-readable description of the tracking event",
            example = "Package received at Mumbai sorting facility and is being processed")
    private String description;

    @Schema(description = "Timestamp of the tracking event (defaults to now if not provided)",
            example = "2026-06-15T10:30:00")
    private LocalDateTime eventTimestamp;
}
