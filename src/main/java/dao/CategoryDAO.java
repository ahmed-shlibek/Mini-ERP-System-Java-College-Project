package main.java.dao;

import main.java.model.Category;

import java.sql.SQLException;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface CategoryDAO {
    Category save(Category category) throws SQLException;

    Optional<Category> findById(UUID uuid) throws SQLException;

    Optional<Category> findByName(String name) throws SQLException;

    List<Category> findAll() throws SQLException;

    void delete(UUID uuid) throws SQLException;
}
