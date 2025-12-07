package main.java.controller;

import main.java.model.Category;
import main.java.model.Product;
import main.java.service.CategoryService;
import main.java.service.InventoryService;

import javax.swing.*;
import java.util.List;
import java.util.UUID;

/**
 * متحكم المنتجات (Controller Layer).
 * يربط بين واجهة المستخدم (UI) ومنطق الأعمال (ProductService).
 */
public class InventoryController {

    private final InventoryService inventoryService;
    private final CategoryService categoryService;

    public InventoryController(InventoryService inventoryService, CategoryService categoryService) {
        this.inventoryService = inventoryService;
        this.categoryService = categoryService;
    }

    // ------------------ عمليات العرض ------------------

    /**
     * جلب قائمة جميع المنتجات.
     */
    public List<Product> getAllProducts() {
        return inventoryService.getAllProducts(); // نفترض وجود هذه الدالة في الخدمة
    }

    /**
     * جلب قائمة جميع الأصناف لعرضها في قائمة منسدلة.
     */
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories(); // نفترض وجود هذه الدالة في الخدمة
    }

    // ------------------ عمليات الإدارة (للأدمين) ------------------

    /**
     * إنشاء منتج جديد.
     */
    public Product createProduct(String name, long price, int stockQuantity, UUID categoryId) throws Exception {
        // يتم التحقق من الصلاحيات والتفاصيل داخل Service
        return inventoryService.createProduct(new Product(name, categoryId, price, stockQuantity));
    }

    /**
     * تحديث كمية المخزون لمنتج معين.
     */
    public void updateProductStock(UUID productId, int newQuantity) throws Exception {
        // يتم استخدام ProductService هنا
        inventoryService.updateProductQuantity(productId, newQuantity);
    }

    // يمكن إضافة دالات أخرى مثل updateProductDetails, deleteProduct...
}