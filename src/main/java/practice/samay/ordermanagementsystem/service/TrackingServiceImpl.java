package practice.samay.ordermanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.dao.ShipmentDao;
import practice.samay.ordermanagementsystem.dao.TrackingDao;
import practice.samay.ordermanagementsystem.dto.request.TrackingRequest;
import practice.samay.ordermanagementsystem.dto.response.TrackingResponse;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;
import practice.samay.ordermanagementsystem.exception.ResourceNotFoundException;
import practice.samay.ordermanagementsystem.model.Shipment;
import practice.samay.ordermanagementsystem.model.Tracking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Tracking event business operations.
 * Adds tracking checkpoints and propagates status back to the Shipment.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrackingServiceImpl implements TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingServiceImpl.class);

    private final TrackingDao trackingDao;
    private final ShipmentDao shipmentDao;

    /**
     * Adds a tracking event to a shipment.
     * Updates the shipment's status and timestamps accordingly.
     */
    @Override
    @Transactional
    public TrackingResponse addTrackingEvent(TrackingRequest request) {
        log.info("Adding tracking event for shipment id: {} | status: {}", request.getShipmentId(), request.getStatus());

        Shipment shipment = shipmentDao.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id: " + request.getShipmentId()));

        LocalDateTime eventTime = (request.getEventTimestamp() != null)
                ? request.getEventTimestamp()
                : LocalDateTime.now();

        Tracking tracking = Tracking.builder()
                .shipmentId(request.getShipmentId())
                .location(request.getLocation())
                .status(request.getStatus())
                .description(request.getDescription())
                .eventTimestamp(eventTime)
                .build();

        Tracking savedTracking = trackingDao.save(tracking);

        // Propagate status to shipment
        if (request.getStatus() != null) {
            shipment.setStatus(request.getStatus());

            if (request.getStatus() == ShipmentStatus.DISPATCHED && shipment.getShippedAt() == null) {
                shipment.setShippedAt(LocalDateTime.now());
            }
            if (request.getStatus() == ShipmentStatus.DELIVERED && shipment.getDeliveredAt() == null) {
                shipment.setDeliveredAt(LocalDateTime.now());
            }
            shipmentDao.update(shipment);
        }

        log.info("Tracking event added for shipment {} at location: {}", shipment.getTrackingNumber(), request.getLocation());
        return toResponse(savedTracking, shipment.getTrackingNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse getTrackingById(Long id) {
        log.debug("Fetching tracking event by id: {}", id);
        Tracking tracking = trackingDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking event not found with id: " + id));
        String trackingNumber = shipmentDao.findById(tracking.getShipmentId())
                .map(Shipment::getTrackingNumber).orElse(null);
        return toResponse(tracking, trackingNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingResponse> getTrackingByShipmentId(Long shipmentId) {
        log.debug("Fetching all tracking events for shipment id: {}", shipmentId);
        Shipment shipment = shipmentDao.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));
        return trackingDao.findByShipmentId(shipmentId).stream()
                .map(t -> toResponse(t, shipment.getTrackingNumber()))
                .collect(Collectors.toList());
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private TrackingResponse toResponse(Tracking tracking, String trackingNumber) {
        return TrackingResponse.builder()
                .id(tracking.getId())
                .shipmentId(tracking.getShipmentId())
                .trackingNumber(trackingNumber)
                .location(tracking.getLocation())
                .status(tracking.getStatus() != null ? tracking.getStatus().name() : null)
                .description(tracking.getDescription())
                .eventTimestamp(tracking.getEventTimestamp())
                .createdAt(tracking.getCreatedAt())
                .build();
    }
}
