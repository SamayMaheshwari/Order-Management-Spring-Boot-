package practice.samay.ordermanagementsystem.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.Shipment;

import java.util.List;
import java.util.Optional;

/**
 * Hibernate SessionFactory-based implementation of shipment persistence operations.
 */
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShipmentDaoImpl {

    private static final Logger log = LoggerFactory.getLogger(ShipmentDaoImpl.class);

    private final SessionFactory sessionFactory;

    public Shipment save(Shipment shipment) {
        log.debug("DAO: Persisting new shipment with tracking: {}", shipment.getTrackingNumber());
        sessionFactory.getCurrentSession().persist(shipment);
        return shipment;
    }

    public Shipment update(Shipment shipment) {
        log.debug("DAO: Merging shipment with id: {}", shipment.getId());
        return sessionFactory.getCurrentSession().merge(shipment);
    }

    public Optional<Shipment> findById(Long id) {
        log.debug("DAO: Finding shipment by id: {}", id);
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(Shipment.class, id)
        );
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        log.debug("DAO: Finding shipment by tracking number: {}", trackingNumber);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Shipment s WHERE s.trackingNumber = :trackingNumber", Shipment.class)
                .setParameter("trackingNumber", trackingNumber)
                .uniqueResultOptional();
    }

    public List<Shipment> findByOrderId(Long orderId) {
        log.debug("DAO: Finding shipments for order id: {}", orderId);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Shipment s WHERE s.orderId = :orderId ORDER BY s.createdAt DESC", Shipment.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<Shipment> findAll() {
        log.debug("DAO: Fetching all shipments");
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Shipment s ORDER BY s.createdAt DESC", Shipment.class)
                .getResultList();
    }
}
