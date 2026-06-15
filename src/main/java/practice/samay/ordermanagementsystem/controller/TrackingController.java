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
import practice.samay.ordermanagementsystem.dto.request.TrackingRequest;
import practice.samay.ordermanagementsystem.dto.response.ApiResponse;
import practice.samay.ordermanagementsystem.dto.response.TrackingResponse;
import practice.samay.ordermanagementsystem.service.TrackingServiceImpl;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Tracking", description = "Shipment Tracking APIs – Add and retrieve real-time shipment tracking events")
public class TrackingController {

    private static final Logger log = LoggerFactory.getLogger(TrackingController.class);

    private final TrackingServiceImpl trackingService;

    //  CREATE

    @PostMapping
    @Operation(
        summary = "Add a tracking event",
        description = "Adds a new tracking checkpoint event to a shipment. " +
                      "Automatically updates the shipment status and sets shipped/delivered timestamps. " +
                      "Tracking events are ordered chronologically by eventTimestamp."
    )
    public ResponseEntity<ApiResponse<TrackingResponse>> addTrackingEvent(
            @Valid @RequestBody TrackingRequest request) {
        log.info("POST /api/v1/tracking – shipment id: {} | location: {}", request.getShipmentId(), request.getLocation());
        TrackingResponse response = trackingService.addTrackingEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tracking event added successfully", response));
    }

    //  READ

    @GetMapping("/{id}")
    @Operation(
        summary = "Get tracking event by ID",
        description = "Retrieves a specific tracking event by its database ID."
    )
    public ResponseEntity<ApiResponse<TrackingResponse>> getTrackingById(
            @Parameter(description = "Tracking event database ID", example = "1")
            @PathVariable Long id) {
        log.debug("GET /api/v1/tracking/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Tracking event retrieved successfully",
                trackingService.getTrackingById(id)));
    }

    @GetMapping("/shipment/{shipmentId}")
    @Operation(
        summary = "Get all tracking events for a shipment",
        description = "Retrieves the complete chronological tracking history for a shipment, " +
                      "ordered by event timestamp ascending (earliest first)."
    )
    public ResponseEntity<ApiResponse<List<TrackingResponse>>> getTrackingByShipmentId(
            @Parameter(description = "Shipment ID", example = "1")
            @PathVariable Long shipmentId) {
        log.debug("GET /api/v1/tracking/shipment/{}", shipmentId);
        List<TrackingResponse> events = trackingService.getTrackingByShipmentId(shipmentId);
        return ResponseEntity.ok(ApiResponse.success(
                "Tracking history retrieved (" + events.size() + " events)", events));
    }
}
