package com.example.payment.activity;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentActivityImpl implements PaymentActivity {

    private static final Logger logger = LoggerFactory.getLogger(PaymentActivityImpl.class);

    @Override
    public boolean processPayment(String orderId, Double amount, boolean simulateFailure) {
        logger.info("Processing payment for orderId: {}, amount: {}, simulateFailure: {}", orderId, amount, simulateFailure);

        if (simulateFailure) {
            int attempt = Activity.getExecutionContext().getInfo().getAttempt();
            logger.warn("Simulating payment failure on attempt {} for orderId {}", attempt, orderId);
            throw new RuntimeException("Deliberate payment system failure!");
        }

        logger.info("Payment processed successfully for orderId: {}", orderId);
        return true;
    }
}
