package practice.samay.ordermanagementsystem.dao;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public class InventoryDaoImpl extends GenericDao<Inventory> {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryDaoImpl.class);


    public InventoryDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory, Inventory.class);
    }

    public Optional<Inventory> findByProductCode(String productCode) {

        log.debug("DAO: Finding inventory by product code: {}", productCode);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Inventory i WHERE i.productCode = :productCode",
                        Inventory.class)
                .setParameter("productCode", productCode)
                .uniqueResultOptional();
    }

    public List<Inventory> findAll() {

        log.debug("DAO: Fetching all inventory records");

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Inventory i ORDER BY i.productCode ASC",
                        Inventory.class)
                .getResultList();
    }

    public List<Inventory> findLowStock(int threshold) {

        log.debug(
                "DAO: Fetching inventory with available quantity <= {}",
                threshold);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Inventory i WHERE i.availableQuantity <= :threshold " +
                                "ORDER BY i.availableQuantity ASC",
                        Inventory.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
    public boolean existsByProductCode(String productCode) {

        Long count = sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT COUNT(i) FROM Inventory i " +
                                "WHERE i.productCode = :productCode",
                        Long.class)
                .setParameter("productCode", productCode)
                .uniqueResult();

        return count != null && count > 0;
    }


}

