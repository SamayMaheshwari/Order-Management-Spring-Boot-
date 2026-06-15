package practice.samay.ordermanagementsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a Tracking event returned to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "TrackingResponse", description = "Tracking event details returned in API responses")
public class TrackingResponse {

    @Schema(description = "Unique tracking event ID", example = "1")
    private Long id;

    @Schema(description = "Associated shipment ID", example = "1")
    private Long shipmentId;

    @Schema(description = "Shipment tracking number", example = "TRK-1718000000000-5512")
    private String trackingNumber;

    @Schema(description = "Location at this tracking checkpoint", example = "Mumbai Sorting Facility")
    private String location;

    @Schema(description = "Shipment status at this checkpoint", example = "IN_TRANSIT")
    private String status;

    @Schema(description = "Description of the tracking event",
            example = "Package received at Mumbai sorting facility")
    private String description;

//    @Schema(description = "Timestamp of the actual tracking event")
//    private LocalDateTime eventTimestamp;
//
//    @Schema(description = "Record creation timestamp")
//    private LocalDateTime createdAt;
}
