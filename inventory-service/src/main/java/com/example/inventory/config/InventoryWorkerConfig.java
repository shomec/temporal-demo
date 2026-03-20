package com.example.inventory.config;

import com.example.inventory.activity.InventoryActivityImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryWorkerConfig {

    private static final Logger logger = LoggerFactory.getLogger(InventoryWorkerConfig.class);

    private final WorkerFactory workerFactory;

    @Value("${temporal.taskqueue}")
    private String taskQueue;

    public InventoryWorkerConfig(WorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
    }

    @PostConstruct
    public void startWorker() {
        logger.info("Starting Inventory Worker on task queue: {}", taskQueue);
        Worker worker = workerFactory.newWorker(taskQueue);
        worker.registerActivitiesImplementations(new InventoryActivityImpl());
        workerFactory.start();
        logger.info("Inventory Worker started successfully.");
    }
}
