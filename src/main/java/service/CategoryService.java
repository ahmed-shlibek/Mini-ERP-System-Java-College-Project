package main.java.service;

import main.java.dao.CategoryDAO;
import main.java.model.Category;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public Category createCategory(Category category) {
        if(category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        String normalizedString = category.getName().trim().toLowerCase();

        try {

            if (categoryDAO.findByName(normalizedString).isPresent()) {
                throw new IllegalArgumentException("Category with name " + category.getName() + " already exists");
            }

            category.setName(normalizedString);
            return categoryDAO.save(category);
        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed during category creation.", e);
        }
    }

    public static Optional<Category> getCategoryById(UUID uuid) {
        if(uuid == null) {
            throw new IllegalArgumentException("Category uuid cannot be null");
        }

        try {
            return categoryDAO.findById(uuid);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while fetching category with ID: " + uuid, e);
        }
    }

    public Optional<Category> getCategoryByName(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Category name cannot be null");
        }

        try {
            return categoryDAO.findByName(name);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while fetching category with name: " + name, e);
        }
    }

    public List<Category> getAllCategories() {
        try {
            return categoryDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while fetching categories", e);
        }
    }

    public void deleteCategory(UUID uuid) {
        if(uuid == null) {
            throw new IllegalArgumentException("Category uuid cannot be null");
        }

        try {
            categoryDAO.delete(uuid);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while deleting category with ID: " + uuid, e);
        }
    }
}
