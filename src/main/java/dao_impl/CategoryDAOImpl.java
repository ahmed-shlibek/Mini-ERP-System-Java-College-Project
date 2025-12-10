package main.java.dao_impl;

import main.java.dao.CategoryDAO;
import main.java.model.Category;
import main.java.database.DBConnection;
import main.java.util.DaoUtil;
import main.java.util.ResultSetMapper;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDateTime;

public class CategoryDAOImpl implements CategoryDAO, ResultSetMapper<Category> {

    private static final String INSERT_CATEGORY_SQL = "INSERT INTO categories (category_id, name) VALUES (?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT category_id, name, created_at FROM categories WHERE category_id = ?";
    private static final String SELECT_BY_NAME_SQL = "SELECT category_id, name, created_at FROM categories WHERE name = ?";
    private static final String SELECT_ALL_SQL = "SELECT category_id, name, created_at FROM categories";
    private static final String DELETE_SQL = "DELETE FROM categories WHERE category_id = ?";

    @Override
    public Category map(ResultSet rs) throws SQLException {
        byte[] idBytes = rs.getBytes("category_id");
        UUID categoryId = DaoUtil.bytesToUUID(idBytes);
        String name = rs.getString("name");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;
        return new Category(categoryId, name, createdAt);
    }

    @Override
    public Category save(Category category) throws SQLException {
        if (category.getCategoryId() == null) {
            category.setCategoryId(UUID.randomUUID());
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_CATEGORY_SQL)) {
            byte[] idBytes = DaoUtil.uuidToBytes(category.getCategoryId());
            stmt.setBytes(1, idBytes);
            stmt.setString(2, category.getName());
            stmt.executeUpdate();
        }

        return category;
    }

    @Override
    public Optional<Category> findById(UUID uuid) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setBytes(1, DaoUtil.uuidToBytes(uuid));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = map(rs);
                    return Optional.of(category);
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Category> findByName(String name) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_NAME_SQL)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = map(rs);
                    return Optional.of(category);
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Category> findAll()  throws SQLException {
        List<Category> categories = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Category category = map(rs);
                categories.add(category);
            }
        }

        return categories;
    }

    @Override
    public void delete(UUID uuid) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(DELETE_SQL)) {
            stmt.setBytes(1, DaoUtil.uuidToBytes(uuid));
            stmt.executeUpdate();
        }
    }
}
