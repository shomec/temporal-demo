package com.example.order.workflow;

import com.example.order.activity.InventoryActivity;
import com.example.order.activity.OrderActivity;
import com.example.order.activity.PaymentActivity;
import com.example.order.activity.ShippingActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class OrderWorkflowImpl implements OrderWorkflow {

    private final Logger logger = Workflow.getLogger(OrderWorkflowImpl.class);

    private String status = "STARTED";

    private final OrderActivity orderActivity = Workflow.newActivityStub(OrderActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumAttempts(3)
                            .build())
                    .build());

    private final InventoryActivity inventoryActivity = Workflow.newActivityStub(InventoryActivity.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue("INVENTORY_TASK_QUEUE")
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(2))
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(3) 
                            .build())
                    .build());

    private final PaymentActivity paymentActivity = Workflow.newActivityStub(PaymentActivity.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue("PAYMENT_TASK_QUEUE")
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(2)) 
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(3) 
                            .build())
                    .build());

    private final ShippingActivity shippingActivity = Workflow.newActivityStub(ShippingActivity.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue("SHIPPING_TASK_QUEUE")
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(2))
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(3) 
                            .build())
                    .build());

    @Override
    public void processOrder(String orderId, Double amount, boolean simulateInventoryFailure, boolean simulatePaymentFailure, boolean simulateShippingFailure) {
        logger.info("Started processOrder SAGA for order {}", orderId);

        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());

        try {
            // Step 1: Reserve Inventory
            status = "RESERVING_INVENTORY";
            inventoryActivity.reserveInventory(orderId, "DEFAULT_ITEM", simulateInventoryFailure);
            saga.addCompensation(inventoryActivity::releaseInventory, orderId, "DEFAULT_ITEM");

            // Step 2: Create in local DB
            status = "CREATING_ORDER";
            orderActivity.createOrderInDb(orderId, amount);
            saga.addCompensation(orderActivity::updateOrderInDb, orderId, "CANCELED_SAGA_ROLLBACK");

            // Step 3: Process Payment
            status = "PROCESSING_PAYMENT";
            paymentActivity.processPayment(orderId, amount, simulatePaymentFailure);
            saga.addCompensation(paymentActivity::refundPayment, orderId, amount);

            // Step 4: Ship Product
            status = "SHIPPING_PRODUCT";
            shippingActivity.shipProduct(orderId, simulateShippingFailure);
            saga.addCompensation(shippingActivity::cancelShipping, orderId);

            // Complete order
            status = "COMPLETED";
            orderActivity.updateOrderInDb(orderId, "COMPLETED");

        } catch (Exception e) {
            status = "FAILED_COMPENSATING";
            logger.error("Workflow failed. Triggering SAGA compensations for order {}", orderId, e);
            
            // Execute all registered compensations in reverse order
            saga.compensate();
            
            status = "CANCELED_DUE_TO_FAILURE";
            logger.info("SAGA compensations completed for order {}", orderId);
            
            throw io.temporal.failure.ApplicationFailure.newFailure("Order workflow failed and compensated", "SAGA_FAILURE");
        }
    }

    @Override
    public String getStatus() {
        return status;
    }
}
