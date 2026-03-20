package com.example.inventory.activity;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InventoryActivityImpl implements InventoryActivity {

    private static final Logger logger = LoggerFactory.getLogger(InventoryActivityImpl.class);

    @Override
    public boolean reserveInventory(String orderId, String item, boolean simulateFailure) {
        logger.info("Reserving inventory for order {}, item {}", orderId, item);
        
        if (simulateFailure) {
            logger.error("Simulated failure during inventory reservation for order {}", orderId);
            throw io.temporal.failure.ApplicationFailure.newFailure("Inventory service unavailable", "SIMULATED_FAILURE");
        }
        
        logger.info("Successfully reserved inventory for order {}, item {}", orderId, item);
        return true;
    }

    @Override
    public void releaseInventory(String orderId, String item) {
        logger.info("COMPENSATION: Releasing reserved inventory for order {}, item {}", orderId, item);
        // Simulation of rollback logic
        logger.info("Inventory released for order {}", orderId);
    }
}
