package main.java.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Order {
    private UUID orderId;
    private UUID userId;
    private String status;
    private LocalDateTime createdAt;

    // default constructor
    public Order() {
        this.status = "PENDING";
    }

    // Constructor for creating a new Order (only user ID is known initially)
    public Order(UUID userId) {
        this.userId = userId;
        this.status = "PENDING";
    }

    // full body constructor
    public Order(UUID orderId, UUID userId, String status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
    }

    // getter functions
    public UUID getUserId() {
        return userId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // setter functions
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
