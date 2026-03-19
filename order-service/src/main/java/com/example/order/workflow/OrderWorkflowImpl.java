package com.example.order.workflow;

import com.example.order.activity.OrderActivity;
import com.example.order.activity.PaymentActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class OrderWorkflowImpl implements OrderWorkflow {

    private final Logger logger = Workflow.getLogger(OrderWorkflowImpl.class);

    private String status = "STARTED";

    // Activities for the Order Service
    private final OrderActivity orderActivity = Workflow.newActivityStub(OrderActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumAttempts(3)
                            .build())
                    .build());

    // Activities for the remote Payment Service.
    // Notice it uses a specific task queue.
    private final PaymentActivity paymentActivity = Workflow.newActivityStub(PaymentActivity.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue("PAYMENT_TASK_QUEUE")
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(2)) // Retry every 2s
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(5) // Will retry 5 times before failing workflow
                            .build())
                    .build());

    @Override
    public void processOrder(String orderId, Double amount, boolean simulatePaymentFailure) {
        logger.info("Started processOrder workflow for order {}", orderId);

        try {
            // Step 1: Create in database
            status = "CREATING_ORDER";
            orderActivity.createOrderInDb(orderId, amount);

            // Step 2: Process Payment (remote activity)
            status = "PROCESSING_PAYMENT";
            paymentActivity.processPayment(orderId, amount, simulatePaymentFailure);

            // Step 3: Complete order
            status = "COMPLETED";
            orderActivity.updateOrderInDb(orderId, "COMPLETED");

        } catch (Exception e) {
            status = "FAILED";
            logger.error("Workflow failed for order {}", orderId, e);
            // Attempt to update DB robustly
            try {
                orderActivity.updateOrderInDb(orderId, "FAILED");
            } catch (Exception updateEx) {
                 logger.error("Failed to update order status to FAILED in DB", updateEx);
            }
            throw Workflow.wrap(e); // Propagate error
        }
    }

    @Override
    public String getStatus() {
        return status;
    }
}
