package practice.samay.ordermanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.cache.InventoryCacheService;
import practice.samay.ordermanagementsystem.cache.OrderCacheService;
import practice.samay.ordermanagementsystem.cache.ShipmentCacheService;
import practice.samay.ordermanagementsystem.dao.InventoryDaoImpl;
import practice.samay.ordermanagementsystem.dao.OrderDaoImpl;
import practice.samay.ordermanagementsystem.dao.ShipmentDaoImpl;
import practice.samay.ordermanagementsystem.dto.request.ShipmentRequest;
import practice.samay.ordermanagementsystem.dto.response.ShipmentResponse;
import practice.samay.ordermanagementsystem.dto.response.OrderResponse;
import practice.samay.ordermanagementsystem.enums.OrderStatus;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;
import practice.samay.ordermanagementsystem.exception.BusinessException;
import practice.samay.ordermanagementsystem.exception.ResourceNotFoundException;
import practice.samay.ordermanagementsystem.model.Order;
import practice.samay.ordermanagementsystem.model.Shipment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShipmentServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(ShipmentServiceImpl.class);

    private final ShipmentDaoImpl shipmentDao;
    private final OrderDaoImpl orderDao;
    private final InventoryDaoImpl inventoryDao;
    private final ShipmentCacheService shipmentCacheService;
    private final OrderCacheService orderCacheService;
    private final InventoryCacheService inventoryCacheService;
    private final HistoryEventPublisher historyEventPublisher;


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
        ShipmentResponse shipmentResponse = toResponse(savedShipment, order.getOrderNumber());
        shipmentCacheService.cacheSnapshot(shipmentResponse);
        shipmentCacheService.evictAll();

        // Move order to PROCESSING state
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.PROCESSING);
            Order updatedOrder = orderDao.update(order);
            OrderResponse orderResponse = toOrderResponse(updatedOrder);
            orderCacheService.cacheSnapshot(orderResponse);
            orderCacheService.evictListCaches();
            historyEventPublisher.publish("ORDER", updatedOrder.getId(), "UPDATE", orderResponse);
        }

        // Deduct reserved inventory (product is now being shipped)
        inventoryCacheService.getByProductCode(order.getProductCode())
                .or(() -> inventoryDao.findByProductCode(order.getProductCode()))
                .ifPresent(inventory -> {
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - order.getQuantity()));
            inventoryDao.update(inventory);
            inventoryCacheService.cacheSnapshot(inventory);
        });

        historyEventPublisher.publish("SHIPMENT", savedShipment.getId(), "CREATE", shipmentResponse);
        inventoryCacheService.evictAll();

        log.info("Shipment created: {} | Carrier: {}", trackingNumber, request.getCarrier());
        return shipmentResponse;
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        log.debug("Fetching shipment by id: {}", id);
        ShipmentResponse cachedShipment = shipmentCacheService.getById(id).orElse(null);
        if (cachedShipment != null) {
            return cachedShipment;
        }
        Shipment shipment = findShipmentOrThrow(id);
        ShipmentResponse shipmentResponse = toResponse(shipment, resolveOrderNumber(shipment.getOrderId()));
        shipmentCacheService.cacheSnapshot(shipmentResponse);
        return shipmentResponse;
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        log.debug("Fetching shipment by tracking number: {}", trackingNumber);
        ShipmentResponse cachedShipment = shipmentCacheService.getByTrackingNumber(trackingNumber).orElse(null);
        if (cachedShipment != null) {
            return cachedShipment;
        }
        Shipment shipment = shipmentDao.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking number: " + trackingNumber));
        ShipmentResponse shipmentResponse = toResponse(shipment, resolveOrderNumber(shipment.getOrderId()));
        shipmentCacheService.cacheSnapshot(shipmentResponse);
        return shipmentResponse;
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByOrderId(Long orderId) {
        log.debug("Fetching shipments for order id: {}", orderId);
        List<ShipmentResponse> cachedShipments = shipmentCacheService.getByOrderId(orderId).orElse(null);
        if (cachedShipments != null) {
            return cachedShipments;
        }
        orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        String orderNumber = resolveOrderNumber(orderId);
        List<ShipmentResponse> shipments = shipmentDao.findByOrderId(orderId).stream()
                .map(s -> toResponse(s, orderNumber))
                .collect(Collectors.toList());
        shipmentCacheService.cacheByOrderId(orderId, shipments);
        return shipments;
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        log.debug("Fetching all shipments");
        return shipmentCacheService.getAll().orElseGet(() -> {
            List<ShipmentResponse> shipments = shipmentDao.findAll().stream()
                .map(s -> toResponse(s, resolveOrderNumber(s.getOrderId())))
                .collect(Collectors.toList());
            shipmentCacheService.cacheAll(shipments);
            return shipments;
        });
    }


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
                    Order updatedOrder = orderDao.update(order);
                    OrderResponse orderResponse = toOrderResponse(updatedOrder);
                    orderCacheService.cacheSnapshot(orderResponse);
                    orderCacheService.evictListCaches();
                    historyEventPublisher.publish("ORDER", updatedOrder.getId(), "UPDATE", orderResponse);
                });
            }

            if (newStatus == ShipmentStatus.DELIVERED) {
                shipment.setDeliveredAt(LocalDateTime.now());
                // Update order status to DELIVERED
                orderDao.findById(shipment.getOrderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.DELIVERED);
                    Order updatedOrder = orderDao.update(order);
                    OrderResponse orderResponse = toOrderResponse(updatedOrder);
                    orderCacheService.cacheSnapshot(orderResponse);
                    orderCacheService.evictListCaches();
                    historyEventPublisher.publish("ORDER", updatedOrder.getId(), "UPDATE", orderResponse);
                    log.info("Order {} marked as DELIVERED.", order.getOrderNumber());
                });
            }

            Shipment updated = shipmentDao.update(shipment);
            ShipmentResponse shipmentResponse = toResponse(updated, resolveOrderNumber(shipment.getOrderId()));
            shipmentCacheService.cacheSnapshot(shipmentResponse);
            shipmentCacheService.evictAll();
            historyEventPublisher.publish("SHIPMENT", updated.getId(), "UPDATE", shipmentResponse);
            log.info("Shipment {} status updated to: {}", id, newStatus);
            return shipmentResponse;
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid shipment status: " + status +
                    ". Valid values: PREPARING, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED");
        }
    }

    // ─── Private Helpers

    private Shipment findShipmentOrThrow(Long id) {
        return shipmentDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
    }

    private String resolveOrderNumber(Long orderId) {
        return orderDao.findById(orderId).map(Order::getOrderNumber).orElse(null);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .build();
    }


    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    private ShipmentResponse toResponse(Shipment shipment, String orderNumber) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status(shipment.getStatus().name())
                .shippingAddress(shipment.getShippingAddress())
                .weight(shipment.getWeight())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .build();
    }

}
