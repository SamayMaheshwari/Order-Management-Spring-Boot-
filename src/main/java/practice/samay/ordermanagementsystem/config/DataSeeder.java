package practice.samay.ordermanagementsystem.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.dao.InventoryDaoImpl;
import practice.samay.ordermanagementsystem.model.Inventory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final InventoryDaoImpl inventoryDao;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking if inventory needs seeding...");
        
        List<Inventory> existingInventory = inventoryDao.findAll();
        if (existingInventory.isEmpty()) {
            log.info("Inventory is empty! Seeding default products...");

            List<Inventory> defaultProducts = Arrays.asList(
                    Inventory.builder()
                        .productCode("PROD-001")
                        .productName("Wireless Bluetooth Headphones")
                            .availableQuantity(50)
                            .reservedQuantity(0)
                            .reorderLevel(5)
                        .unitPrice(new BigDecimal("2999.99"))
                            .build(),
                    Inventory.builder()
                        .productCode("PROD-002")
                        .productName("Smart Phone Pro Max")
                            .availableQuantity(100)
                            .reservedQuantity(0)
                            .reorderLevel(10)
                        .unitPrice(new BigDecimal("55000.00"))
                            .build(),
                    Inventory.builder()
                        .productCode("PROD-003")
                            .productName("Wireless Noise Cancelling Headphones")
                            .availableQuantity(150)
                            .reservedQuantity(0)
                            .reorderLevel(15)
                        .unitPrice(new BigDecimal("15000.00"))
                            .build(),
                    Inventory.builder()
                        .productCode("PROD-004")
                            .productName("Fitness Tracking Smart Watch")
                            .availableQuantity(80)
                            .reservedQuantity(0)
                            .reorderLevel(8)
                        .unitPrice(new BigDecimal("8000.00"))
                        .build(),
                    Inventory.builder()
                        .productCode("PROD-005")
                        .productName("Wireless Bluetooth Headphones Pro")
                        .availableQuantity(60)
                        .reservedQuantity(0)
                        .reorderLevel(5)
                        .unitPrice(new BigDecimal("3499.99"))
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
