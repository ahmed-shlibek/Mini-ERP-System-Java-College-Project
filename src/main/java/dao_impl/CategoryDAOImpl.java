package main.java.dao_impl;

import main.java.dao.CategoryDAO;
import main.java.model.Category;
import main.java.database.DBConnection;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDateTime;

public class CategoryDAOImpl implements CategoryDAO {

    private static final String INSERT_CATEGORY_SQL = "INSERT INTO categories (category_id, name) VALUES (?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT category_id, name, created_at FROM categories WHERE category_id = ?";
    private static final String SELECT_BY_NAME_SQL = "SELECT category_id, name, created_at FROM categories WHERE name = ?";
    private static final String SELECT_ALL_SQL = "SELECT category_id, name, created_at FROM categories";
    private static final String DELETE_SQL = "DELETE FROM categories WHERE category_id = ?";

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

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        byte[] idBytes = rs.getBytes("category_id");
        UUID categoryId = bytesToUUID(idBytes);
        String name = rs.getString("name");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;
        return new Category(categoryId, name, createdAt);
    }

    @Override
    public Category save(Category category) {
        if (category.getCategoryId() == null) {
            category.setCategoryId(UUID.randomUUID());
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CATEGORY_SQL)) {
            byte[] idBytes = uuidToBytes(category.getCategoryId());
            stmt.setBytes(1, idBytes);
            stmt.setString(2, category.getName());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Database error while saving category: " + e.getMessage());
        }
        return category;
    }

    @Override
    public Optional<Category> findById(UUID uuid) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setObject(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = mapResultSetToCategory(rs);
                    return Optional.of(category);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while finding category by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_NAME_SQL)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = mapResultSetToCategory(rs);
                    return Optional.of(category);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Database error while finding category by name: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Category category = mapResultSetToCategory(rs);
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Database error while finding all categories: " + e.getMessage());
        }
        return categories;
    }

    @Override
    public void delete(UUID uuid) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(DELETE_SQL)) {
            stmt.setObject(1, uuid);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Category with ID " + uuid + " deleted successfully.");
            } else {
                System.out.println("Category with ID " + uuid + " not found or already deleted.");
            }
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                System.err.println("Database Error: Cannot delete category. Products are still linked to it.");
            } else {
                System.err.println("Database error while deleting category: " + e.getMessage());
            }
        }
    }
}
