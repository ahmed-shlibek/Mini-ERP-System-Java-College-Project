package main.java.controller;

import main.java.model.Category;
import main.java.model.Product;
import main.java.service.CategoryService;
import main.java.service.InventoryService;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
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
    public Product updateProductDetails(
            UUID productId,
            Long newPriceInCents,
            Integer newQuantity) {

        if (productId == null) {
            throw new IllegalArgumentException("Product ID must be provided for update.");
        }

        // Basic check: at least one field must be provided for update
        if (newPriceInCents == null && newQuantity == null) {
            throw new IllegalArgumentException("Must provide a new price, a new quantity, or both for the update.");
        }

        try {
            // Check for the most efficient path: Stock Update ONLY
            // This path avoids fetching the product and using the full update logic.
            boolean isOnlyQuantityUpdate = (newPriceInCents == null && newQuantity != null);

            if (isOnlyQuantityUpdate) {
                // SCENARIO A: Only Quantity (Stock) Update
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("Quantity update cannot be negative.");
                }

                // Call the specialized service method (which performs the stock update efficiently)
                inventoryService.updateProductQuantity(productId, newQuantity);

                // Fetch and return the result
                return inventoryService.getProductById(productId).orElseThrow(
                        () -> new RuntimeException("Update succeeded, but product could not be fetched.")
                );

            } else {
                // SCENARIO B: Price Update, or Price and Quantity Update

                // 1. Fetch the existing product to get current state (e.g., name, categoryId, etc.)
                Optional<Product> optionalProduct = inventoryService.getProductById(productId);
                if (optionalProduct.isEmpty()) {
                    throw new IllegalArgumentException("Product with ID " + productId + " not found.");
                }
                Product productToUpdate = optionalProduct.get();

                // 2. Apply updates ONLY if the new value is provided (not null)
                if (newPriceInCents != null) {
                    productToUpdate.setPrice(newPriceInCents);
                }
                if (newQuantity != null) {
                    productToUpdate.setQuantity(newQuantity);
                }

                // 3. Call the comprehensive service method.
                // The service will handle all necessary validation (non-negativity for price/quantity, name/category validation from the existing product).
                return inventoryService.updateProduct(productToUpdate);
            }
        } catch (Exception e) {
            // Centralized exception handling
            throw new RuntimeException("Controller failed to process product update: " + e.getMessage(), e);
        }
    }

    public void deleteProduct(UUID productId) throws Exception {
        // Delegates the deletion request to the Service Layer
        inventoryService.deleteProduct(productId);
    }

    // يمكن إضافة دالات أخرى مثل updateProductDetails, deleteProduct...
}