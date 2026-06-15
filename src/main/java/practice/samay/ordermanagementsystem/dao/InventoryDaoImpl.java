package practice.samay.ordermanagementsystem.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.Inventory;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InventoryDaoImpl {

    private static final Logger log = LoggerFactory.getLogger(InventoryDaoImpl.class);

    private final SessionFactory sessionFactory;

    public Inventory save(Inventory inventory) {
        log.debug("DAO: Persisting inventory for product: {}", inventory.getProductCode());
        sessionFactory.getCurrentSession().persist(inventory);
        return inventory;
    }

    public Inventory update(Inventory inventory) {
        log.debug("DAO: Merging inventory for product: {}", inventory.getProductCode());
        return sessionFactory.getCurrentSession().merge(inventory);
    }

    public Optional<Inventory> findById(Long id) {
        log.debug("DAO: Finding inventory by id: {}", id);
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(Inventory.class, id)
        );
    }

    public Optional<Inventory> findByProductCode(String productCode) {
        log.debug("DAO: Finding inventory by product code: {}", productCode);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Inventory i WHERE i.productCode = :productCode", Inventory.class)
                .setParameter("productCode", productCode)
                .uniqueResultOptional();
    }

    public List<Inventory> findAll() {
        log.debug("DAO: Fetching all inventory records");
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Inventory i ORDER BY i.productCode ASC", Inventory.class)
                .getResultList();
    }

    public List<Inventory> findLowStock(int threshold) {
        log.debug("DAO: Fetching inventory with available quantity <= {}", threshold);
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Inventory i WHERE i.availableQuantity <= :threshold ORDER BY i.availableQuantity ASC",
                        Inventory.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
}
