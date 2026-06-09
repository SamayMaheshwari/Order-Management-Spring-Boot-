package practice.samay.ordermanagementsystem.dao;

import practice.samay.ordermanagementsystem.model.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Inventory entity operations.
 * Implementations use Hibernate SessionFactory directly.
 */
public interface InventoryDao {

    Inventory save(Inventory inventory);

    Inventory update(Inventory inventory);

    Optional<Inventory> findById(Long id);

    Optional<Inventory> findByProductCode(String productCode);

    List<Inventory> findAll();

    List<Inventory> findLowStock(int threshold);
}
