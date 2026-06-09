package practice.samay.ordermanagementsystem.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.enums.OrderStatus;
import practice.samay.ordermanagementsystem.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Hibernate SessionFactory-based implementation of OrderDao.
 * All operations use getCurrentSession() and run within @Transactional boundaries
 * defined in the Service layer.
 */
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderDaoImpl implements OrderDao {

    private static final Logger log = LoggerFactory.getLogger(OrderDaoImpl.class);

    private final SessionFactory sessionFactory;

    @Override
    public Order save(Order order) {
        log.debug("DAO: Persisting new order with number: {}", order.getOrderNumber());
        sessionFactory.getCurrentSession().persist(order);
        return order;
    }

    @Override
    public Order update(Order order) {
        log.debug("DAO: Merging order with id: {}", order.getId());
        return sessionFactory.getCurrentSession().merge(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        log.debug("DAO: Finding order by id: {}", id);
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(Order.class, id)
        );
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        log.debug("DAO: Finding order by number: {}", orderNumber);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Order o WHERE o.orderNumber = :orderNumber", Order.class)
                .setParameter("orderNumber", orderNumber)
                .uniqueResultOptional();
    }

    @Override
    public List<Order> findAll() {
        log.debug("DAO: Fetching all orders");
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Order o ORDER BY o.createdAt DESC", Order.class)
                .getResultList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        log.debug("DAO: Fetching orders with status: {}", status);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC", Order.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Order> findByCustomerEmail(String email) {
        log.debug("DAO: Fetching orders for customer email: {}", email);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Order o WHERE o.customerEmail = :email ORDER BY o.createdAt DESC", Order.class)
                .setParameter("email", email)
                .getResultList();
    }

    @Override
    public void deleteById(Long id) {
        log.debug("DAO: Deleting order with id: {}", id);
        findById(id).ifPresent(order ->
                sessionFactory.getCurrentSession().remove(order)
        );
    }
}
