package practice.samay.ordermanagementsystem.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.Tracking;

import java.util.List;
import java.util.Optional;

/**
 * Hibernate SessionFactory-based implementation of tracking persistence operations.
 */
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrackingDaoImpl {

    private static final Logger log = LoggerFactory.getLogger(TrackingDaoImpl.class);

    private final SessionFactory sessionFactory;

    public Tracking save(Tracking tracking) {
        log.debug("DAO: Persisting tracking event for shipment id: {}", tracking.getShipmentId());
        sessionFactory.getCurrentSession().persist(tracking);
        return tracking;
    }

    public Optional<Tracking> findById(Long id) {
        log.debug("DAO: Finding tracking event by id: {}", id);
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(Tracking.class, id)
        );
    }

    public List<Tracking> findByShipmentId(Long shipmentId) {
        log.debug("DAO: Finding tracking events for shipment id: {}", shipmentId);
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Tracking t WHERE t.shipmentId = :shipmentId ORDER BY t.eventTimestamp ASC",
                        Tracking.class)
                .setParameter("shipmentId", shipmentId)
                .getResultList();
    }
}
