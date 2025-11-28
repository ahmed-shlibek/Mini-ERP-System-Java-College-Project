package main.java.service;

import main.java.dao.CategoryDAO;
import main.java.model.Category;

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

        if (category.getName() == null || category.getName().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null");
        }

        String normalizedString = category.getName().toLowerCase();

        if (categoryDAO.findByName(normalizedString).isPresent()) {
            throw new IllegalArgumentException("Category with name " + category.getName() + " already exists");
        }

        category.setName(normalizedString);

        return categoryDAO.save(category);
    }

    public Optional<Category> getCategoryById(UUID uuid) {
        if(uuid == null) {
            throw new IllegalArgumentException("Category uuid cannot be null");
        }

        return categoryDAO.findById(uuid);
    }

    public Optional<Category> getCategoryByName(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Category name cannot be null");
        }

        return categoryDAO.findByName(name);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public void deleteCategory(UUID uuid) {
        if(uuid == null) {
            throw new IllegalArgumentException("Category uuid cannot be null");
        }

        categoryDAO.delete(uuid);
    }
}
