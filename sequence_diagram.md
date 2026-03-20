# Temporal SAGA Sequence Diagram

Below is a detailed sequence diagram showing the interactions between the REST API, Temporal, the Workflow Orchestrator, and the Activity Workers. It covers both the standard expected execution (Happy Path) and the compensating rollback flow (Failure Path).

```mermaid
sequenceDiagram
    autonumber
    
    actor Client
    participant API as Order Controller
    participant Temporal as Temporal Server
    participant Workflow as Order Workflow
    participant Inv as Inventory Activity
    participant OrderDB as Order DB Activity
    participant Pay as Payment Activity
    participant Ship as Shipping Activity

    %% --------------------------------
    %% HAPPY PATH
    %% --------------------------------
    rect rgb(240, 248, 255)
    Note over Client, Ship: SCENARIO 1: HAPPY PATH (SUCCESSFUL SAGA EXECUTION)
    
    Client->>API: POST /api/orders (amount: 50.0)
    API->>Temporal: StartWorkflowExecution('OrderFlow-100')
    Temporal-->>API: RunId
    API-->>Client: HTTP 202 Accepted (Order ID: 100)
    
    Temporal->>Workflow: Execute processOrder()
    
    %% Step 1
    Workflow->>Inv: reserveInventory(orderId)
    Inv-->>Workflow: Success
    Note over Workflow, Temporal: Saga.addCompensation(releaseInventory)
    
    %% Step 2
    Workflow->>OrderDB: createOrderInDb(orderId, amount)
    OrderDB-->>Workflow: Success
    Note over Workflow, Temporal: Saga.addCompensation(updateOrderInDb: "CANCELED_SAGA_ROLLBACK")
    
    %% Step 3
    Workflow->>Pay: processPayment(orderId, amount)
    Pay-->>Workflow: Success
    Note over Workflow, Temporal: Saga.addCompensation(refundPayment)
    
    %% Step 4
    Workflow->>Ship: shipProduct(orderId)
    Ship-->>Workflow: Success
    Note over Workflow, Temporal: Saga.addCompensation(cancelShipping)
    
    %% Completion
    Workflow->>OrderDB: updateOrderInDb(orderId, "COMPLETED")
    OrderDB-->>Workflow: Success
    Workflow-->>Temporal: Complete Workflow Execution
    end

    %% --------------------------------
    %% FAILURE PATH (SAGA ROLLBACK)
    %% --------------------------------
    rect rgb(255, 240, 240)
    Note over Client, Ship: SCENARIO 2: SAGA COMPENSATIONS (PAYMENT FAILS)
    
    Client->>API: POST /api/orders?simulatePaymentFailure=true
    API->>Temporal: StartWorkflowExecution('OrderFlow-200')
    API-->>Client: HTTP 202 Accepted
    
    Temporal->>Workflow: Execute processOrder()
    
    %% Step 1 & 2 Succeed
    Workflow->>Inv: reserveInventory(orderId)
    Inv-->>Workflow: Success
    Workflow->>OrderDB: createOrderInDb(orderId, amount)
    OrderDB-->>Workflow: Success
    
    %% Step 3 Fails
    Workflow->>Pay: processPayment(orderId) (Simulating Failure)
    Note over Pay, Temporal: Payment gateway throws RuntimeException
    Pay-->>xTemporal: ActivityTaskFailed
    
    note over Temporal, Pay: Temporal automatically retries up to MaximumAttempts (3)
    Temporal-->>xWorkflow: ApplicationFailure (Max Attempts Exhausted)
    
    %% Saga Compensation Block
    Note over Workflow: Exception caught! Executing saga.compensate()
    
    %% Reverse Order DB
    Workflow->>OrderDB: updateOrderInDb("CANCELED_SAGA_ROLLBACK")
    OrderDB-->>Workflow: Success
    
    %% Reverse Inventory
    Workflow->>Inv: releaseInventory(orderId)
    Inv-->>Workflow: Success
    
    %% End Workflow
    Workflow-->>Temporal: Fail Workflow Execution (CANCELED_DUE_TO_FAILURE)
    end
```
