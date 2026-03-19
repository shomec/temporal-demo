package com.example.order.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "status")
    private String status;

    public OrderEntity() {}

    public OrderEntity(String id, Double amount, String status) {
        this.id = id;
        this.amount = amount;
        this.status = status;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
