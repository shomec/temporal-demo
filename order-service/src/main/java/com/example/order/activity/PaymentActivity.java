package com.example.order.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface PaymentActivity {

    @ActivityMethod
    boolean processPayment(String orderId, Double amount, boolean simulateFailure);
}
