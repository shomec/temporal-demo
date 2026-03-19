package com.example.order.activity;

import com.example.order.entity.OrderEntity;
import com.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderActivityImpl implements OrderActivity {

    private static final Logger logger = LoggerFactory.getLogger(OrderActivityImpl.class);
    private final OrderRepository orderRepository;

    public OrderActivityImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void createOrderInDb(String orderId, Double amount) {
        logger.info("Creating order {} in DB with amount {}", orderId, amount);
        OrderEntity order = new OrderEntity(orderId, amount, "PENDING");
        orderRepository.save(order);
    }

    @Override
    public void updateOrderInDb(String orderId, String status) {
        logger.info("Updating order {} in DB to status {}", orderId, status);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }
}
