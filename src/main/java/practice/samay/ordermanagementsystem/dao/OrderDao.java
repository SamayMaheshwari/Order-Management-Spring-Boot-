package practice.samay.ordermanagementsystem.dao;

import practice.samay.ordermanagementsystem.enums.OrderStatus;
import practice.samay.ordermanagementsystem.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Order entity operations.
 * Implementations use Hibernate SessionFactory directly.
 */
public interface OrderDao {

    Order save(Order order);

    Order update(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String email);

    void deleteById(Long id);
}
