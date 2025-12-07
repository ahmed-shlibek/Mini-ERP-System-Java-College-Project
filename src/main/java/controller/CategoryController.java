package main.java.controller;

import main.java.model.Category;
import main.java.service.CategoryService;
import java.util.List;
import java.util.UUID;

/**
 * متحكم الأصناف (Controller Layer).
 * يربط بين الواجهة ومنطق الأعمال لإدارة الأصناف.
 */
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * جلب قائمة جميع الأصناف.
     */
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    /**
     * إنشاء صنف جديد بالاسم المحدد.
     */
    public Category createCategory(String name) throws Exception {
        // نفترض وجود دالة createCategory في CategoryService
        return categoryService.createCategory(new Category(name));
    }

    // يمكن إضافة دالة deleteCategory لاحقاً
}