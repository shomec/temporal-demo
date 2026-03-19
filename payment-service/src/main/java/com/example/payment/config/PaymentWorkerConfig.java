package com.example.payment.config;

import com.example.payment.activity.PaymentActivityImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentWorkerConfig {

    private static final Logger logger = LoggerFactory.getLogger(PaymentWorkerConfig.class);

    private final WorkerFactory workerFactory;

    @Value("${temporal.taskqueue}")
    private String taskQueue;

    public PaymentWorkerConfig(WorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
    }

    @PostConstruct
    public void startWorker() {
        logger.info("Starting Payment Worker on task queue: {}", taskQueue);
        Worker worker = workerFactory.newWorker(taskQueue);
        worker.registerActivitiesImplementations(new PaymentActivityImpl());
        workerFactory.start();
        logger.info("Payment Worker started successfully.");
    }
}
