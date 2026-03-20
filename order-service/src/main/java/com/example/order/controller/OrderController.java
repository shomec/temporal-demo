package com.example.order.controller;

import com.example.order.entity.OrderEntity;
import com.example.order.repository.OrderRepository;
import com.example.order.workflow.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final WorkflowClient workflowClient;
    private final OrderRepository orderRepository;

    public OrderController(WorkflowClient workflowClient, OrderRepository orderRepository) {
        this.workflowClient = workflowClient;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request,
                                         @RequestParam(required = false, defaultValue = "false") boolean simulateInventoryFailure,
                                         @RequestParam(required = false, defaultValue = "false") boolean simulatePaymentFailure,
                                         @RequestParam(required = false, defaultValue = "false") boolean simulateShippingFailure) {
        String orderId = (String) request.get("orderId");
        Double amount = request.get("amount") != null ? Double.valueOf(request.get("amount").toString()) : 0.0;

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue("ORDER_TASK_QUEUE")
                // Adding WorkflowId Reuse Policy is a good practice, but setting ID is enough for demo
                .setWorkflowId("OrderFlow-" + orderId)
                .build();

        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, options);
        WorkflowClient.start(workflow::processOrder, orderId, amount, simulateInventoryFailure, simulatePaymentFailure, simulateShippingFailure);

        return ResponseEntity.accepted().body("SAGA started with ID: " + orderId 
            + ". Failures[Inv:" + simulateInventoryFailure + ", Pay:" + simulatePaymentFailure + ", Ship:" + simulateShippingFailure + "]");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId,
                                      @RequestParam(required = false, defaultValue = "false") boolean simulateDbFailure) {
        if (simulateDbFailure) {
            throw new RuntimeException("Simulated Database Connection Exception! Cannot fetch order.");
        }

        Optional<OrderEntity> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{orderId}/workflow-status")
    public ResponseEntity<?> getWorkflowStatus(@PathVariable String orderId) {
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, "OrderFlow-" + orderId);
        try {
            String status = workflow.getStatus();
            return ResponseEntity.ok(Map.of("workflowStatus", status));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error querying workflow: " + e.getMessage());
        }
    }
}
