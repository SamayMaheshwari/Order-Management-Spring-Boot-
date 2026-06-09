package practice.samay.ordermanagementsystem.dao;

import practice.samay.ordermanagementsystem.model.Shipment;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Shipment entity operations.
 * Implementations use Hibernate SessionFactory directly.
 */
public interface ShipmentDao {

    Shipment save(Shipment shipment);

    Shipment update(Shipment shipment);

    Optional<Shipment> findById(Long id);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByOrderId(Long orderId);

    List<Shipment> findAll();
}
