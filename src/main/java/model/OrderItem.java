package main.java.model;

import java.util.UUID;

public class OrderItem {
    private UUID orderId;
    private UUID productId;
    private long priceAtOrder;
    private int quantity;
    private String productName;

    public OrderItem() {

    }

    public OrderItem(UUID orderId, UUID productId) {
        this.orderId = orderId;
        this.productId = productId;
    }
    public OrderItem(UUID orderId, String productName, int quantity){
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
    }

    public OrderItem(UUID orderId, UUID productId, long priceAtOrder, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.priceAtOrder = priceAtOrder;
        this.quantity = quantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public long getPriceAtOrder() {
        return priceAtOrder;
    }

    public int getQuantity() {
        return quantity;
    }

    //i added this m
    public String getProductName() { // <--- 3. NEW GETTER
        return productName;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setPriceAtOrder(long priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductName(String productName) { // <--- 4. NEW SETTER
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderId=" + orderId +
                ", productId=" + productId +
                ", priceAtOrder=" + priceAtOrder +
                ", quantity=" + quantity +
                '}';
    }
}
