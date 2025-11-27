package main.java.dao;

import main.java.model.OrderItem;

import java.util.List;
import java.util.UUID;

public interface OrderItemDAO {
    void saveAll(List<OrderItem> items);

    List<OrderItem> findByOrderId(UUID orderId);

    void update(OrderItem orderItem);

    // delete order with all items
    void deleteByOrderId(UUID orderId);

    // delete item in order
    void deleteByKeys(UUID orderId, UUID productId);
}
