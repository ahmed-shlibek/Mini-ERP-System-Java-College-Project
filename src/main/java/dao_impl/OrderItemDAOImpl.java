package main.java.dao_impl;

import main.java.dao.OrderItemDAO;
import main.java.database.DBConnection;
import main.java.model.OrderItem;
import main.java.util.DaoUtil;
import main.java.util.ResultSetMapper;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderItemDAOImpl implements OrderItemDAO, ResultSetMapper<OrderItem> {

    private static final String INSERT_SQL = "INSERT INTO order_items (order_id, product_id, price_at_order, quantity) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ORDER_ID_SQL = "SELECT order_id, product_id, price_at_order, quantity FROM order_items WHERE order_id = ?";
    private static final String UPDATE_SQL = "UPDATE order_items SET price_at_order = ?, quantity = ? WHERE order_id = ? AND product_id = ?";
    private static final String DELETE_BY_ORDER_ID_SQL = "DELETE FROM order_items WHERE order_id = ?";
    private static final String DELETE_BY_KEYS_SQL = "DELETE FROM order_items WHERE order_id = ? AND product_id = ?";

    @Override
    public OrderItem map(ResultSet rs) throws SQLException {
        byte[] orderIdBytes = rs.getBytes("order_id");
        UUID orderId = DaoUtil.bytesToUUID(orderIdBytes);
        byte[] productIdBytes = rs.getBytes("product_id");
        UUID productId = DaoUtil.bytesToUUID(productIdBytes);
        long priceAtOrder = rs.getLong("price_at_order");
        int quantity = rs.getInt("quantity");
        return new OrderItem(orderId, productId, priceAtOrder, quantity);
    }

    @Override
    public void saveAll(List<OrderItem> items) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
                for (OrderItem item : items) {

                    if (item.getOrderId() == null || item.getProductId() == null) {
                        continue;
                    }

                    stmt.setBytes(1, DaoUtil.uuidToBytes(item.getOrderId()));
                    stmt.setBytes(2, DaoUtil.uuidToBytes(item.getProductId()));
                    stmt.setLong(3, item.getPriceAtOrder());
                    stmt.setInt(4, item.getQuantity());

                    stmt.addBatch();
                }

                stmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                }
            } catch (SQLException ex) {

            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) throws SQLException {
        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ORDER_ID_SQL)) {

            stmt.setBytes(1, DaoUtil.uuidToBytes(orderId));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = map(rs);
                    orderItems.add(item);
                }
            }
        }
        return orderItems;
    }

    @Override
    public OrderItem update(OrderItem orderItem) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setLong(1, orderItem.getPriceAtOrder());
            stmt.setInt(2, orderItem.getQuantity());
            stmt.setBytes(3, DaoUtil.uuidToBytes(orderItem.getOrderId()));
            stmt.setBytes(4, DaoUtil.uuidToBytes(orderItem.getProductId()));

            stmt.executeUpdate();
        }

        return orderItem;
    }

    @Override
    public void deleteByOrderId(UUID orderId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ORDER_ID_SQL)) {

            stmt.setBytes(1, DaoUtil.uuidToBytes(orderId));

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteByKeys(UUID orderId, UUID productId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_KEYS_SQL)) {

            stmt.setBytes(1, DaoUtil.uuidToBytes(orderId));
            stmt.setBytes(2, DaoUtil.uuidToBytes(productId));

            stmt.executeUpdate();
        }
    }
}