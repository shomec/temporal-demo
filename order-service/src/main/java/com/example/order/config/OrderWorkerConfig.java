package com.example.order.config;

import com.example.order.activity.OrderActivityImpl;
import com.example.order.workflow.OrderWorkflowImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkerConfig {

    private static final Logger logger = LoggerFactory.getLogger(OrderWorkerConfig.class);

    private final WorkerFactory workerFactory;
    private final OrderActivityImpl orderActivityImpl;

    @Value("${temporal.taskqueue}")
    private String taskQueue;

    public OrderWorkerConfig(WorkerFactory workerFactory, OrderActivityImpl orderActivityImpl) {
        this.workerFactory = workerFactory;
        this.orderActivityImpl = orderActivityImpl;
    }

    @PostConstruct
    public void startWorker() {
        logger.info("Starting Order Worker on task queue: {}", taskQueue);
        Worker worker = workerFactory.newWorker(taskQueue);
        // Register workflow implementation class
        worker.registerWorkflowImplementationTypes(OrderWorkflowImpl.class);
        // Register activity implementation instance (since it calls Spring beans)
        worker.registerActivitiesImplementations(orderActivityImpl);

        workerFactory.start();
        logger.info("Order Worker started successfully.");
    }
}
