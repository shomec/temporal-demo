package com.example.order.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderActivity {

    @ActivityMethod
    void createOrderInDb(String orderId, Double amount);

    @ActivityMethod
    void updateOrderInDb(String orderId, String status);
}
