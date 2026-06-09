package practice.samay.ordermanagementsystem.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.dao.InventoryDao;
import practice.samay.ordermanagementsystem.model.Inventory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final InventoryDao inventoryDao;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking if inventory needs seeding...");
        
        List<Inventory> existingInventory = inventoryDao.findAll();
        if (existingInventory.isEmpty()) {
            log.info("Inventory is empty! Seeding default products...");

            List<Inventory> defaultProducts = Arrays.asList(
                    Inventory.builder()
                            .productCode("LAP-001")
                            .productName("Premium Performance Laptop v2")
                            .availableQuantity(50)
                            .reservedQuantity(0)
                            .reorderLevel(5)
                            .unitPrice(new BigDecimal("85000.00"))
                            .build(),
                    Inventory.builder()
                            .productCode("PHN-002")
                            .productName("Smart Phone Pro Max")
                            .availableQuantity(100)
                            .reservedQuantity(0)
                            .reorderLevel(10)
                            .unitPrice(new BigDecimal("55000.00"))
                            .build(),
                    Inventory.builder()
                            .productCode("HDPH-003")
                            .productName("Wireless Noise Cancelling Headphones")
                            .availableQuantity(150)
                            .reservedQuantity(0)
                            .reorderLevel(15)
                            .unitPrice(new BigDecimal("15000.00"))
                            .build(),
                    Inventory.builder()
                            .productCode("WATCH-004")
                            .productName("Fitness Tracking Smart Watch")
                            .availableQuantity(80)
                            .reservedQuantity(0)
                            .reorderLevel(8)
                            .unitPrice(new BigDecimal("8000.00"))
                            .build()
            );

            for (Inventory item : defaultProducts) {
                inventoryDao.save(item);
                log.info("Seeded product: {}", item.getProductCode());
            }
            log.info("Inventory seeding completed successfully!");
        } else {
            log.info("Inventory already contains {} items. Skipping seeding.", existingInventory.size());
        }
    }
}
