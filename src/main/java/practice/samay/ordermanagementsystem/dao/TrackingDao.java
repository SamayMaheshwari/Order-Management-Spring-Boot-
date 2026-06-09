package practice.samay.ordermanagementsystem.dao;

import practice.samay.ordermanagementsystem.model.Tracking;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Tracking entity operations.
 * Implementations use Hibernate SessionFactory directly.
 */
public interface TrackingDao {

    Tracking save(Tracking tracking);

    Optional<Tracking> findById(Long id);

    List<Tracking> findByShipmentId(Long shipmentId);
}
