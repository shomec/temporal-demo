package com.example.shipping.activity;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShippingActivityImpl implements ShippingActivity {

    private static final Logger logger = LoggerFactory.getLogger(ShippingActivityImpl.class);

    @Override
    public boolean shipProduct(String orderId, boolean simulateFailure) {
        logger.info("Shipping order {}", orderId);
        
        if (simulateFailure) {
            logger.error("Simulated shipping failure for order {}", orderId);
            throw io.temporal.failure.ApplicationFailure.newFailure("Shipping Provider Error", "SHIPPING_ERROR");
        }

        logger.info("Order {} shipped successfully", orderId);
        return true;
    }

    @Override
    public void cancelShipping(String orderId) {
        logger.info("COMPENSATION: Canceling shipping for order {}", orderId);
        // Simulation of rollback logic
        logger.info("Shipping canceled for order {}", orderId);
    }
}
