package practice.samay.ordermanagementsystem.service;

import practice.samay.ordermanagementsystem.dto.request.OrderRequest;
import practice.samay.ordermanagementsystem.dto.response.OrderResponse;

import java.util.List;


public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByStatus(String status);

    OrderResponse updateOrderStatus(Long id, String status);

    void cancelOrder(Long id);
}
