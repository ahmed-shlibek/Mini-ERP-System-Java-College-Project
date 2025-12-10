package main.java.dao_impl;

import main.java.dao.OrderDAO;
import main.java.dao.OrderItemDAO;
import main.java.database.DBConnection;
import main.java.model.Order;
import main.java.util.DaoUtil;
import main.java.util.ResultSetMapper;

import java.nio.ByteBuffer;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderDAOImpl implements OrderDAO, ResultSetMapper<Order> {

    private static final String INSERT_ORDER_SQL = "INSERT INTO orders(order_id, user_id) VALUES (?, ?)";
    private static final String SELECT_BY_ORDER_ID_SQL = "SELECT order_id, user_id,status ,created_at FROM orders WHERE order_id = ?";
    private static final String SELECT_BY_USER_ID_SQL = "SELECT order_id, user_id,status ,created_at FROM orders WHERE user_id = ?";
    private static final String SELECT_BY_DATE_SQL = "SELECT order_id, user_id,status ,created_at FROM orders WHERE created_at BETWEEN ? AND ?";
    private static final String SELECT_ALL_SQL = "SELECT order_id, user_id,status ,created_at FROM orders";
    private static final String UPDATE_SQL = "UPDATE orders SET status = ? WHERE order_id = ?";
    private static final String DELETE_SQL = "DELETE FROM orders WHERE order_id = ?";
    private static final String SELECT_ITEMS_BY_ORDER_ID_SQL = "SELECT order_id, product_id, price_at_order, quantity FROM order_items WHERE order_id = ?";

    private final OrderItemDAO orderItemDAO;

    public OrderDAOImpl(OrderItemDAO orderItemDAO) {
        this.orderItemDAO = orderItemDAO;
    }

    @Override
    public Order map(ResultSet rs) throws SQLException {
        byte[] orderIdBytes = rs.getBytes("order_id");
        UUID orderId = DaoUtil.bytesToUUID(orderIdBytes);
        byte[] userIdBytes = rs.getBytes("user_id");
        UUID userId = DaoUtil.bytesToUUID(userIdBytes);
        String status = rs.getString("status");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;
        return new Order(orderId, userId, status, createdAt);
    }

    @Override
    public Order save(Order order) throws SQLException {
        if(order.getOrderId() == null) {
            order.setOrderId(UUID.randomUUID());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ORDER_SQL)) {
            byte[] orderIdBytes = DaoUtil.uuidToBytes(order.getOrderId());
            stmt.setBytes(1, orderIdBytes);
            byte[] userIdBytes = DaoUtil.uuidToBytes(order.getUserId());
            stmt.setBytes(2, userIdBytes);

            stmt.executeUpdate();
        }

        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId)  throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ORDER_ID_SQL)) {

            stmt.setBytes(1, DaoUtil.uuidToBytes(orderId));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = map(rs);
                    order.setOrderItems(orderItemDAO.findByOrderId(orderId));
                    return Optional.of(order);
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findByUserId(UUID userId) throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER_ID_SQL)) {

            stmt.setBytes(1, DaoUtil.uuidToBytes(userId));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = map(rs);
                order.setOrderItems(orderItemDAO.findByOrderId(order.getOrderId()));
                orders.add(order);
            }

        }

        return orders;
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DATE_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = map(rs);
                order.setOrderItems(orderItemDAO.findByOrderId(order.getOrderId()));
                orders.add(order);
            }
        }

        return orders;
    }

    @Override
    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = map(rs);
                order.setOrderItems(orderItemDAO.findByOrderId(order.getOrderId()));
                orders.add(order);
            }
        }

        return orders;
    }

    @Override
    public Order update(Order order) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, order.getStatus());
            stmt.setBytes(2, DaoUtil.uuidToBytes(order.getOrderId()));
            stmt.executeUpdate();
        }

        return order;
    }

    @Override
    public void delete(UUID orderId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setBytes(1, DaoUtil.uuidToBytes(orderId));
            stmt.executeUpdate();
        }
    }
}