package practice.samay.ordermanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.cache.ShipmentCacheService;
import practice.samay.ordermanagementsystem.cache.TrackingCacheService;
import practice.samay.ordermanagementsystem.dao.OrderDaoImpl;
import practice.samay.ordermanagementsystem.dao.ShipmentDaoImpl;
import practice.samay.ordermanagementsystem.dao.TrackingDaoImpl;
import practice.samay.ordermanagementsystem.dto.request.TrackingRequest;
import practice.samay.ordermanagementsystem.dto.response.TrackingResponse;
import practice.samay.ordermanagementsystem.dto.response.ShipmentResponse;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;
import practice.samay.ordermanagementsystem.exception.ResourceNotFoundException;
import practice.samay.ordermanagementsystem.model.Order;
import practice.samay.ordermanagementsystem.model.Shipment;
import practice.samay.ordermanagementsystem.model.Tracking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrackingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(TrackingServiceImpl.class);

    private final TrackingDaoImpl trackingDao;
    private final ShipmentDaoImpl shipmentDao;
    private final OrderDaoImpl orderDao;
    private final TrackingCacheService trackingCacheService;
    private final ShipmentCacheService shipmentCacheService;
    private final HistoryEventPublisher historyEventPublisher;

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
            Shipment updatedShipment = shipmentDao.update(shipment);
            ShipmentResponse shipmentResponse = toShipmentResponse(updatedShipment, resolveOrderNumber(updatedShipment.getOrderId()));
            shipmentCacheService.cacheSnapshot(shipmentResponse);
            historyEventPublisher.publish("SHIPMENT", updatedShipment.getId(), "UPDATE", shipmentResponse);
        }

        TrackingResponse trackingResponse = toResponse(savedTracking, shipment.getTrackingNumber());
        trackingCacheService.cacheSnapshot(trackingResponse);
        historyEventPublisher.publish("TRACKING", savedTracking.getId(), "CREATE", trackingResponse);
        log.info("Tracking event added for shipment {} at location: {}", shipment.getTrackingNumber(), request.getLocation());
        return trackingResponse;
    }

    @Transactional(readOnly = true)
    public TrackingResponse getTrackingById(Long id) {
        log.debug("Fetching tracking event by id: {}", id);
        TrackingResponse cachedTracking = trackingCacheService.getById(id).orElse(null);
        if (cachedTracking != null) {
            return cachedTracking;
        }
        Tracking tracking = trackingDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking event not found with id: " + id));
        String trackingNumber = shipmentDao.findById(tracking.getShipmentId())
                .map(Shipment::getTrackingNumber).orElse(null);
        TrackingResponse trackingResponse = toResponse(tracking, trackingNumber);
        trackingCacheService.cacheSnapshot(trackingResponse);
        return trackingResponse;
    }

    @Transactional(readOnly = true)
    public List<TrackingResponse> getTrackingByShipmentId(Long shipmentId) {
        log.debug("Fetching all tracking events for shipment id: {}", shipmentId);
        List<TrackingResponse> cachedTrackingEvents = trackingCacheService.getByShipmentId(shipmentId).orElse(null);
        if (cachedTrackingEvents != null) {
            return cachedTrackingEvents;
        }
        Shipment shipment = shipmentDao.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));
        List<TrackingResponse> trackingResponses = trackingDao.findByShipmentId(shipmentId).stream()
                .map(t -> toResponse(t, shipment.getTrackingNumber()))
                .collect(Collectors.toList());
        trackingCacheService.cacheByShipmentId(shipmentId, trackingResponses);
        return trackingResponses;
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
                .build();
    }


    private String resolveOrderNumber(Long orderId) {
        return orderDao.findById(orderId).map(Order::getOrderNumber).orElse(null);
    }

    private ShipmentResponse toShipmentResponse(Shipment shipment, String orderNumber) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status(shipment.getStatus().name())
                .shippingAddress(shipment.getShippingAddress())
                .weight(shipment.getWeight())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .build();
    }


}
