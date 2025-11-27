package main.java.dao;

import main.java.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderDAO {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findByUserId(UUID userId);

    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findAll();

    void update(Order order);

    void delete(UUID orderId);
}
