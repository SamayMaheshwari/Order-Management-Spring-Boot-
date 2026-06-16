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
import practice.samay.ordermanagementsystem.dto.request.OrderRequest;
import practice.samay.ordermanagementsystem.dto.response.ApiResponse;
import practice.samay.ordermanagementsystem.dto.response.OrderResponse;
import practice.samay.ordermanagementsystem.service.OrderServiceImpl;

import java.util.List;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Orders", description = "Order Management APIs – Create, retrieve, update and cancel orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderServiceImpl orderService;

    //  CREATE

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new customer order after validating product availability in inventory. " +
                      "Stock is reserved on order creation. Returns 409 if insufficient stock."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request) {
        log.info("POST /api/v1/orders – customer: {}", request.getCustomerEmail());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    // READ

    @GetMapping
    @Operation(
        summary = "Get all orders",
        description = "Retrieves all orders sorted by creation date descending."
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        log.debug("GET /api/v1/orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved successfully (" + orders.size() + " records)", orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its database ID.")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @Parameter(description = "Order database ID", example = "1")
            @PathVariable Long id) {
        log.debug("GET /api/v1/orders/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrderById(id)));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieves a specific order by its unique order number (e.g. ORD-...).")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @Parameter(description = "Order number", example = "ORD-1718000000000-4231")
            @PathVariable String orderNumber) {
        log.debug("GET /api/v1/orders/number/{}", orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrderByOrderNumber(orderNumber)));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get orders by status",
        description = "Retrieves all orders matching the given status. " +
                      "Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @Parameter(description = "Order status filter", example = "CONFIRMED")
            @PathVariable String status) {
        log.debug("GET /api/v1/orders/status/{}", status);
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved for status: " + status.toUpperCase(), orders));
    }

    //  DELETE

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel an order",
        description = "Cancels an order and restores the reserved inventory. " +
                      "Orders in SHIPPED or DELIVERED status cannot be cancelled."
    )
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @Parameter(description = "Order ID to cancel", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/v1/orders/{} – cancelling order", id);
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully and inventory restored", null));
    }

}
