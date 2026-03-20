package com.example.inventory.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface InventoryActivity {

    @ActivityMethod
    boolean reserveInventory(String orderId, String item, boolean simulateFailure);

    @ActivityMethod
    void releaseInventory(String orderId, String item);
}
