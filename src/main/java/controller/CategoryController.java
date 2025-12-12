package main.java.controller;

import main.java.model.Category;
import main.java.service.CategoryService;
import java.util.List;
import java.util.UUID;


public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //List to get all categories
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }


    public Category createCategory(String name) throws Exception {
        // نفترض وجود دالة createCategory في CategoryService
        return categoryService.createCategory(new Category(name));
    }

    public void deleteCategory(UUID categoryId) throws Exception {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID must be provided for deletion.");
        }
        categoryService.deleteCategory(categoryId);
    }
    // يمكن إضافة دالة deleteCategory لاحقاً
}