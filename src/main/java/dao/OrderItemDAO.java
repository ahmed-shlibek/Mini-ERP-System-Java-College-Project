package main.java.dao;

import main.java.model.OrderItem;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface OrderItemDAO {
    void saveAll(List<OrderItem> items) throws SQLException;

    List<OrderItem> findByOrderId(UUID orderId) throws SQLException;

    OrderItem update(OrderItem orderItem) throws SQLException;

    // delete order with all items
    void deleteByOrderId(UUID orderId) throws SQLException;

    // delete item in order
    void deleteByKeys(UUID orderId, UUID productId) throws SQLException;
}
