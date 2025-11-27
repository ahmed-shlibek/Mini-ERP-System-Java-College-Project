package main.java.dao_impl;

import main.java.dao.OrderDAO;
import main.java.database.DBConnection;
import main.java.model.Order;
import java.nio.ByteBuffer;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderDAOImpl implements OrderDAO {

    private static final String INSERT_ORDER_SQL = "INSERT INTO orders(order_id, user_id) VALUES (?, ?)";
    private static final String SELECT_BY_Order_ID_SQL = "SELECT order_id, user_id,status ,created_at FROM orders WHERE order_id = ?";
    private static final String SELECT_BY_USER_ID_SQL = "SELECT order_id, user_id,status ,created_at FROM orders WHERE user_id = ?";
    private static final String SELECT_BY_DATE_SQL = "SELECT order_id, user_id,status ,created_at FROM orders WHERE created_at BETWEEN ? AND ?";
    private static final String SELECT_ALL_SQL = "SELECT order_id, user_id,status ,created_at FROM orders";
    private static final String UPDATE_SQL = "UPDATE orders SET status = ? WHERE order_id = ?";
    private static final String DELETE_SQL = "DELETE FROM orders WHERE order_id = ?";

    private byte[] uuidToBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private UUID bytesToUUID(byte[] bytes) {
        if (bytes == null || bytes.length < 16) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        byte[] orderIdBytes = rs.getBytes("order_id");
        UUID orderId = bytesToUUID(orderIdBytes);
        byte[] userIdBytes = rs.getBytes("user_id");
        UUID userId = bytesToUUID(userIdBytes);
        String status = rs.getString("status");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;
        return new Order(orderId, userId, status, createdAt);
    }

    @Override
    public Order save(Order order) {
        if(order.getOrderId() == null) {
            order.setOrderId(UUID.randomUUID());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ORDER_SQL)) {
            byte[] orderIdBytes = uuidToBytes(order.getOrderId());
            stmt.setBytes(1, orderIdBytes);
            byte[] userIdBytes = uuidToBytes(order.getUserId());
            stmt.setBytes(2, userIdBytes);

            int  affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

        } catch (SQLException e) {
            System.err.println("Database error while saving order: " + e.getMessage());
        }
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_Order_ID_SQL)) {

            stmt.setBytes(1, uuidToBytes(orderId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                return Optional.of(order);
            }

            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("Database error while fetching order: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER_ID_SQL)) {

            stmt.setBytes(1, uuidToBytes(userId));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }

        } catch (SQLException e) {
            System.err.println("Database error while fetching orders: " + e.getMessage());
        }

        return orders;
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DATE_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching orders: " + e.getMessage());
        }

        return orders;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching orders: " + e.getMessage());
        }

        return orders;
    }

    @Override
    public void update(Order order) {
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, order.getStatus());
            stmt.setBytes(2, uuidToBytes(order.getOrderId()));
            int affectedRows = stmt.executeUpdate();

            if(affectedRows == 0){
                System.err.println("Warning rows affected :"+ affectedRows + "the order id:"+order.getOrderId());
            }

        } catch (SQLException e) {
            System.err.println("Database error while updating order: " + e.getMessage());
        }
    }

    @Override
    public void delete(UUID orderId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setBytes(1, uuidToBytes(orderId));
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Order with ID " + orderId + " deleted successfully.");
            } else {
                System.out.println("Order with ID " + orderId + " not found or already deleted.");
            }

        } catch (SQLException e) {
            System.err.println("Database error while deleting order: " + e.getMessage());
        }
    }
}