package practice.samay.ordermanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.cache.InventoryCacheService;
import practice.samay.ordermanagementsystem.cache.OrderCacheService;
import practice.samay.ordermanagementsystem.dao.InventoryDao;
import practice.samay.ordermanagementsystem.dao.OrderDao;
import practice.samay.ordermanagementsystem.dto.request.OrderRequest;
import practice.samay.ordermanagementsystem.dto.response.OrderResponse;
import practice.samay.ordermanagementsystem.enums.OrderStatus;
import practice.samay.ordermanagementsystem.exception.BusinessException;
import practice.samay.ordermanagementsystem.exception.InsufficientStockException;
import practice.samay.ordermanagementsystem.exception.ResourceNotFoundException;
import practice.samay.ordermanagementsystem.model.Inventory;
import practice.samay.ordermanagementsystem.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderDao orderDao;
    private final InventoryDao inventoryDao;
    private final OrderCacheService orderCacheService;
    private final InventoryCacheService inventoryCacheService;
    private final HistoryEventPublisher historyEventPublisher;


    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {} | product: {}", request.getCustomerEmail(), request.getProductCode());

        // Validate inventory existence
        Inventory inventory = inventoryCacheService.getByProductCode(request.getProductCode())
            .or(() -> inventoryDao.findByProductCode(request.getProductCode()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in inventory: " + request.getProductCode()));

        inventoryCacheService.cacheSnapshot(inventory);

        // Validate sufficient stock
        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            log.warn("Insufficient stock for product: {}. Available: {}, Requested: {}",
                    request.getProductCode(), inventory.getAvailableQuantity(), request.getQuantity());
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + request.getProductCode() +
                    "'. Available: " + inventory.getAvailableQuantity() +
                    ", Requested: " + request.getQuantity());
        }

        // Calculate total amount
        BigDecimal totalAmount = request.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        // Build and persist order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Order savedOrder = orderDao.save(order);

        // Reserve inventory stock
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        inventoryDao.update(inventory);

        OrderResponse orderResponse = toResponse(savedOrder);
        orderCacheService.cacheSnapshot(orderResponse);
        orderCacheService.evictListCaches();
        inventoryCacheService.cacheSnapshot(inventory);
        historyEventPublisher.publish("ORDER", savedOrder.getId(), "CREATE", orderResponse);
        historyEventPublisher.publish("INVENTORY", inventory.getId(), "UPDATE", inventory);

        log.info("Order created successfully: {} | Total: ₹{}", savedOrder.getOrderNumber(), totalAmount);
        return orderResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order by id: {}", id);
        OrderResponse cachedOrder = orderCacheService.getById(id).orElse(null);
        if (cachedOrder != null) {
            return cachedOrder;
        }
        OrderResponse orderResponse = toResponse(findOrderOrThrow(id));
        orderCacheService.cacheSnapshot(orderResponse);
        return orderResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by number: {}", orderNumber);
        OrderResponse cachedOrder = orderCacheService.getByOrderNumber(orderNumber).orElse(null);
        if (cachedOrder != null) {
            return cachedOrder;
        }

        OrderResponse orderResponse = toResponse(orderDao.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber)));
        orderCacheService.cacheSnapshot(orderResponse);
        return orderResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders");
        return orderCacheService.getAll().orElseGet(() -> {
            List<OrderResponse> orders = orderDao.findAll().stream().map(this::toResponse).collect(Collectors.toList());
            orderCacheService.cacheAll(orders);
            return orders;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderCacheService.getByStatus(orderStatus).orElseGet(() -> {
                List<OrderResponse> orders = orderDao.findByStatus(orderStatus).stream().map(this::toResponse).collect(Collectors.toList());
                orderCacheService.cacheByStatus(orderStatus, orders);
                return orders;
            });
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: " + status);
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        log.info("Updating order {} status to: {}", id, status);
        Order order = findOrderOrThrow(id);
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            Order updated = orderDao.update(order);
            OrderResponse orderResponse = toResponse(updated);
            orderCacheService.cacheSnapshot(orderResponse);
            orderCacheService.evictListCaches();
            historyEventPublisher.publish("ORDER", updated.getId(), "UPDATE", orderResponse);
            log.info("Order {} status updated to: {}", id, newStatus);
            return orderResponse;
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: " + status +
                    ". Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED");
        }
    }


    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);
        Order order = findOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Cannot cancel order in status: " + order.getStatus() +
                    ". Only PENDING, CONFIRMED, or PROCESSING orders can be cancelled.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled.");
        }

        // Restore reserved inventory
        inventoryCacheService.getByProductCode(order.getProductCode())
                .or(() -> inventoryDao.findByProductCode(order.getProductCode()))
                .ifPresent(inventory -> {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + order.getQuantity());
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - order.getQuantity()));
            inventoryDao.update(inventory);
            inventoryCacheService.cacheSnapshot(inventory);
            log.debug("Restored {} units to inventory for product: {}", order.getQuantity(), order.getProductCode());
        });

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderDao.update(order);
        OrderResponse orderResponse = toResponse(updated);
        orderCacheService.cacheSnapshot(orderResponse);
        orderCacheService.evictListCaches();
        historyEventPublisher.publish("ORDER", updated.getId(), "UPDATE", orderResponse);
        log.info("Order {} cancelled successfully", order.getOrderNumber());
    }

    //  Private Helpers

    private Order findOrderOrThrow(Long id) {
        return orderDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .productCode(order.getProductCode())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
