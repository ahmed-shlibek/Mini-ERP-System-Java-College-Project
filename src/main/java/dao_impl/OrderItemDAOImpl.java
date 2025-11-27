package main.java.dao_impl;

import main.java.dao.OrderItemDAO;
import main.java.database.DBConnection;
import main.java.model.Order;
import main.java.model.OrderItem;

import java.nio.ByteBuffer;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderItemDAOImpl implements OrderItemDAO {

    private static final String INSERT_SQL = "INSERT INTO order_items (order_id, product_id, price_at_order, quantity) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ORDER_ID_SQL = "SELECT order_id, product_id, price_at_order, quantity FROM order_items WHERE order_id = ?";
    private static final String UPDATE_SQL = "UPDATE order_items SET price_at_order = ?, quantity = ? WHERE order_id = ? AND product_id = ?";
    private static final String DELETE_BY_ORDER_ID_SQL = "DELETE FROM order_items WHERE order_id = ?";
    private static final String DELETE_BY_KEYS_SQL = "DELETE FROM order_items WHERE order_id = ? AND product_id = ?";



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

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        byte[] orderIdBytes = rs.getBytes("order_id");
        UUID orderId = bytesToUUID(orderIdBytes);
        byte[] productIdBytes = rs.getBytes("product_id");
        UUID productId = bytesToUUID(productIdBytes);
        long priceAtOrder = rs.getLong("price_at_order");
        int quantity = rs.getInt("quantity");
        return new OrderItem(orderId, productId, priceAtOrder, quantity);
    }

    @Override
    public void saveAll(List<OrderItem> items) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            conn.setAutoCommit(false);

            for (OrderItem item : items) {

                if (item.getOrderId() == null || item.getProductId() == null) {
                    System.err.println("Skipping item due to missing Order or Product ID.");
                    continue;
                }

                stmt.setBytes(1, uuidToBytes(item.getOrderId()));
                stmt.setBytes(2, uuidToBytes(item.getProductId()));
                stmt.setLong(3, item.getPriceAtOrder());
                stmt.setInt(4, item.getQuantity());

                stmt.addBatch();
            }

            int[] updateCounts = stmt.executeBatch();

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Database error during batch insert of Order Items: " + e.getMessage());
            try {
                if (DBConnection.getConnection() != null) {
                    DBConnection.getConnection().rollback();
                    System.out.println("Database rollback successful.");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
        }
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ORDER_ID_SQL)) {

            stmt.setBytes(1, uuidToBytes(orderId));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderItem item = mapResultSetToOrderItem(rs);
                orderItems.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Database error during select by Order ID: " + e.getMessage());
        }
        return orderItems;
    }

    @Override
    public void update(OrderItem orderItem) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setLong(1, orderItem.getPriceAtOrder());
            stmt.setInt(2, orderItem.getQuantity());
            stmt.setBytes(3, uuidToBytes(orderItem.getOrderId()));
            stmt.setBytes(4, uuidToBytes(orderItem.getProductId()));

            int affectedRows = stmt.executeUpdate();

            if(affectedRows == 0){
                System.err.println("Warning rows affected :"+ affectedRows + "the orderItem id:"+orderItem.getOrderId());
            }

        } catch (SQLException e) {
            System.err.println("Database error during update of Order Item: " + e.getMessage());
        }
    }

    @Override
    public void deleteByOrderId(UUID orderId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ORDER_ID_SQL)) {

            stmt.setBytes(1, uuidToBytes(orderId));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("OrderItem deleted successfully.");
            } else {
                System.out.println("OrderItem not found or already deleted.");
            }

        } catch (SQLException e) {
            System.err.println("Database error during delete of Order Item: " + e.getMessage());
        }
    }

    @Override
    public void deleteByKeys(UUID orderId, UUID productId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_KEYS_SQL)) {

            stmt.setBytes(1, uuidToBytes(orderId));
            stmt.setBytes(2, uuidToBytes(productId));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("OrderItem deleted successfully.");
            } else {
                System.out.println("OrderItem not found or already deleted.");
            }

        } catch (SQLException e) {
            System.err.println("Database error during delete of Order Item: " + e.getMessage());
        }
    }
}
