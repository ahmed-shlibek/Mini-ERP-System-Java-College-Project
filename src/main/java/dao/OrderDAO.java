package main.java.dao;

import main.java.model.Order;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderDAO {
    Order save(Order order) throws SQLException;

    Optional<Order> findById(UUID orderId)  throws SQLException;

    List<Order> findByUserId(UUID userId)   throws SQLException;

    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;

    List<Order> findAll() throws SQLException;

    Order update(Order order)  throws SQLException;

    void delete(UUID orderId)   throws SQLException;
}
