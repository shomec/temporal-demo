package com.example.payment.activity;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentActivityImpl implements PaymentActivity {

    private static final Logger logger = LoggerFactory.getLogger(PaymentActivityImpl.class);

    @Override
    public boolean processPayment(String orderId, Double amount, boolean simulateFailure) {
        logger.info("Processing payment for order {} with amount {}", orderId, amount);

        // Fetch execution info (demonstrating access to Temporal context)
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        
        if (simulateFailure) {
            logger.error("Simulated payment failure for order {}, attempt {}", orderId, attempt);
            // This will cause Temporal to retry based on Workflow retry options
            throw io.temporal.failure.ApplicationFailure.newFailure("Payment Gateway Timeout", "GATEWAY_TIMEOUT");
        }

        logger.info("Payment processed successfully for order {}", orderId);
        return true;
    }

    @Override
    public void refundPayment(String orderId, Double amount) {
        logger.info("COMPENSATION: Refunding payment for order {} for amount {}", orderId, amount);
        // Simulation of rollback logic
        logger.info("Payment refunded successfully for order {}", orderId);
    }
}
