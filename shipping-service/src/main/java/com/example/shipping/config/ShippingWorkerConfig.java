package com.example.shipping.config;

import com.example.shipping.activity.ShippingActivityImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShippingWorkerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ShippingWorkerConfig.class);

    private final WorkerFactory workerFactory;

    @Value("${temporal.taskqueue}")
    private String taskQueue;

    public ShippingWorkerConfig(WorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
    }

    @PostConstruct
    public void startWorker() {
        logger.info("Starting Shipping Worker on task queue: {}", taskQueue);
        Worker worker = workerFactory.newWorker(taskQueue);
        worker.registerActivitiesImplementations(new ShippingActivityImpl());
        workerFactory.start();
        logger.info("Shipping Worker started successfully.");
    }
}
