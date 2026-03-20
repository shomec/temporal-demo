package com.example.shipping.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ShippingActivity {

    @ActivityMethod
    boolean shipProduct(String orderId, boolean simulateFailure);

    @ActivityMethod
    void cancelShipping(String orderId);
}
