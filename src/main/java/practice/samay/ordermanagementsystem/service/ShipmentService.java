package practice.samay.ordermanagementsystem.service;

import practice.samay.ordermanagementsystem.dto.request.ShipmentRequest;
import practice.samay.ordermanagementsystem.dto.response.ShipmentResponse;

import java.util.List;

/**
 * Service interface for Shipment business operations.
 */
public interface ShipmentService {

    ShipmentResponse createShipment(ShipmentRequest request);

    ShipmentResponse getShipmentById(Long id);

    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber);

    List<ShipmentResponse> getShipmentsByOrderId(Long orderId);

    List<ShipmentResponse> getAllShipments();

    ShipmentResponse updateShipmentStatus(Long id, String status);
}
