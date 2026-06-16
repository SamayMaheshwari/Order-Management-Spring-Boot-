package practice.samay.ordermanagementsystem.dao;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.Shipment;

import java.util.List;
import java.util.Optional;

@Repository
public class ShipmentDaoImpl extends GenericDao<Shipment> {

    private static final Logger log =
            LoggerFactory.getLogger(ShipmentDaoImpl.class);

    public ShipmentDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory, Shipment.class);
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {

        log.debug(
                "DAO: Finding shipment by tracking number: {}",
                trackingNumber);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Shipment s WHERE s.trackingNumber = :trackingNumber",
                        Shipment.class)
                .setParameter("trackingNumber", trackingNumber)
                .uniqueResultOptional();
    }

    public List<Shipment> findByOrderId(Long orderId) {

        log.debug("DAO: Finding shipments for order id: {}", orderId);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Shipment s WHERE s.orderId = :orderId " +
                                "ORDER BY s.createdAt DESC",
                        Shipment.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<Shipment> findAll() {

        log.debug("DAO: Fetching all shipments");

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Shipment s ORDER BY s.createdAt DESC",
                        Shipment.class)
                .getResultList();
    }
}


