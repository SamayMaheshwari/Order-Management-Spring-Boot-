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
import practice.samay.ordermanagementsystem.dto.request.ShipmentRequest;
import practice.samay.ordermanagementsystem.dto.response.ApiResponse;
import practice.samay.ordermanagementsystem.dto.response.ShipmentResponse;
import practice.samay.ordermanagementsystem.service.ShipmentService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Shipments", description = "Shipment Management APIs – Create and track order shipments")
public class ShipmentController {

    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);

    private final ShipmentService shipmentService;

    //  CREATE

    @PostMapping
    @Operation(
        summary = "Create a shipment",
        description = "Creates a new shipment for a CONFIRMED or PROCESSING order. " +
                      "Generates a unique tracking number and moves order to PROCESSING status. " +
                      "Deducts reserved inventory on shipment creation."
    )
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @Valid @RequestBody ShipmentRequest request) {
        log.info("POST /api/v1/shipments – order id: {} | carrier: {}", request.getOrderId(), request.getCarrier());
        ShipmentResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment created successfully", response));
    }

    //  READ

    @GetMapping
    @Operation(summary = "Get all shipments", description = "Retrieves all shipments sorted by creation date descending.")
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> getAllShipments() {
        log.debug("GET /api/v1/shipments");
        List<ShipmentResponse> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(ApiResponse.success(
                "Shipments retrieved successfully (" + shipments.size() + " records)", shipments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID", description = "Retrieves a specific shipment by its database ID.")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentById(
            @Parameter(description = "Shipment database ID", example = "1")
            @PathVariable Long id) {
        log.debug("GET /api/v1/shipments/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Shipment retrieved successfully", shipmentService.getShipmentById(id)));
    }

    @GetMapping("/tracking/{trackingNumber}")
    @Operation(
        summary = "Get shipment by tracking number",
        description = "Retrieves a shipment using its auto-generated tracking number (e.g. TRK-...)."
    )
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByTrackingNumber(
            @Parameter(description = "Shipment tracking number", example = "TRK-1718000000000-5512")
            @PathVariable String trackingNumber) {
        log.debug("GET /api/v1/shipments/tracking/{}", trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Shipment retrieved successfully",
                shipmentService.getShipmentByTrackingNumber(trackingNumber)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Get shipments for an order",
        description = "Retrieves all shipments associated with a specific order."
    )
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> getShipmentsByOrderId(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long orderId) {
        log.debug("GET /api/v1/shipments/order/{}", orderId);
        List<ShipmentResponse> shipments = shipmentService.getShipmentsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(
                "Shipments retrieved for order id: " + orderId, shipments));
    }

    //  UPDATE

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update shipment status",
        description = "Updates the status of a shipment. " +
                      "DISPATCHED → sets shippedAt and marks order SHIPPED. " +
                      "DELIVERED → sets deliveredAt and marks order DELIVERED. " +
                      "Valid values: PREPARING, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED"
    )
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateShipmentStatus(
            @Parameter(description = "Shipment ID", example = "1") @PathVariable Long id,
            @Parameter(description = "New shipment status", example = "IN_TRANSIT")
            @RequestParam String status) {
        log.info("PUT /api/v1/shipments/{}/status – new status: {}", id, status);
        return ResponseEntity.ok(ApiResponse.success(
                "Shipment status updated successfully", shipmentService.updateShipmentStatus(id, status)));
    }
}
