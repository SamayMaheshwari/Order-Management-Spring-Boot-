package practice.samay.ordermanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.dao.InventoryDao;
import practice.samay.ordermanagementsystem.dao.OrderDao;
import practice.samay.ordermanagementsystem.dao.ShipmentDao;
import practice.samay.ordermanagementsystem.dto.request.ShipmentRequest;
import practice.samay.ordermanagementsystem.dto.response.ShipmentResponse;
import practice.samay.ordermanagementsystem.enums.OrderStatus;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;
import practice.samay.ordermanagementsystem.exception.BusinessException;
import practice.samay.ordermanagementsystem.exception.ResourceNotFoundException;
import practice.samay.ordermanagementsystem.model.Order;
import practice.samay.ordermanagementsystem.model.Shipment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Shipment business operations.
 * Manages shipment creation, status updates, and inventory deduction.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShipmentServiceImpl implements ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentServiceImpl.class);

    private final ShipmentDao shipmentDao;
    private final OrderDao orderDao;
    private final InventoryDao inventoryDao;

    /**
     * Creates a shipment for a confirmed/processing order.
     * Updates order status to PROCESSING and deducts reserved inventory.
     */
    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request) {
        log.info("Creating shipment for order id: {}", request.getOrderId());

        Order order = orderDao.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new BusinessException(
                    "Shipment can only be created for CONFIRMED or PROCESSING orders. " +
                    "Current order status: " + order.getStatus());
        }

        String trackingNumber = generateTrackingNumber();
        String deliveryAddress = (request.getShippingAddress() != null && !request.getShippingAddress().isBlank())
                ? request.getShippingAddress()
                : order.getShippingAddress();

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .trackingNumber(trackingNumber)
                .carrier(request.getCarrier())
                .status(ShipmentStatus.PREPARING)
                .shippingAddress(deliveryAddress)
                .weight(request.getWeight())
                .estimatedDelivery(request.getEstimatedDelivery())
                .build();

        Shipment savedShipment = shipmentDao.save(shipment);

        // Move order to PROCESSING state
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.PROCESSING);
            orderDao.update(order);
        }

        // Deduct reserved inventory (product is now being shipped)
        inventoryDao.findByProductCode(order.getProductCode()).ifPresent(inventory -> {
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - order.getQuantity()));
            inventoryDao.update(inventory);
        });

        log.info("Shipment created: {} | Carrier: {}", trackingNumber, request.getCarrier());
        return toResponse(savedShipment, order.getOrderNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        log.debug("Fetching shipment by id: {}", id);
        Shipment shipment = findShipmentOrThrow(id);
        return toResponse(shipment, resolveOrderNumber(shipment.getOrderId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        log.debug("Fetching shipment by tracking number: {}", trackingNumber);
        Shipment shipment = shipmentDao.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking number: " + trackingNumber));
        return toResponse(shipment, resolveOrderNumber(shipment.getOrderId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByOrderId(Long orderId) {
        log.debug("Fetching shipments for order id: {}", orderId);
        orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        String orderNumber = resolveOrderNumber(orderId);
        return shipmentDao.findByOrderId(orderId).stream()
                .map(s -> toResponse(s, orderNumber))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        log.debug("Fetching all shipments");
        return shipmentDao.findAll().stream()
                .map(s -> toResponse(s, resolveOrderNumber(s.getOrderId())))
                .collect(Collectors.toList());
    }

    /**
     * Updates shipment status with cascading order status updates.
     * DISPATCHED → sets shippedAt; DELIVERED → sets deliveredAt and marks order DELIVERED.
     */
    @Override
    @Transactional
    public ShipmentResponse updateShipmentStatus(Long id, String status) {
        log.info("Updating shipment {} status to: {}", id, status);
        Shipment shipment = findShipmentOrThrow(id);
        try {
            ShipmentStatus newStatus = ShipmentStatus.valueOf(status.toUpperCase());
            shipment.setStatus(newStatus);

            if (newStatus == ShipmentStatus.DISPATCHED && shipment.getShippedAt() == null) {
                shipment.setShippedAt(LocalDateTime.now());
                // Update order status to SHIPPED
                orderDao.findById(shipment.getOrderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.SHIPPED);
                    orderDao.update(order);
                });
            }

            if (newStatus == ShipmentStatus.DELIVERED) {
                shipment.setDeliveredAt(LocalDateTime.now());
                // Update order status to DELIVERED
                orderDao.findById(shipment.getOrderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.DELIVERED);
                    orderDao.update(order);
                    log.info("Order {} marked as DELIVERED.", order.getOrderNumber());
                });
            }

            Shipment updated = shipmentDao.update(shipment);
            log.info("Shipment {} status updated to: {}", id, newStatus);
            return toResponse(updated, resolveOrderNumber(shipment.getOrderId()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid shipment status: " + status +
                    ". Valid values: PREPARING, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED");
        }
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private Shipment findShipmentOrThrow(Long id) {
        return shipmentDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
    }

    private String resolveOrderNumber(Long orderId) {
        return orderDao.findById(orderId).map(Order::getOrderNumber).orElse(null);
    }

    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    private ShipmentResponse toResponse(Shipment shipment, String orderNumber) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .orderNumber(orderNumber)
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status(shipment.getStatus().name())
                .shippingAddress(shipment.getShippingAddress())
                .weight(shipment.getWeight())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}
