package main.java.controller;

import main.java.model.Category;
import main.java.service.CategoryService;
import main.java.ui.CategoryView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryView categoryView; // المتحكم يعرف الـ View

    public CategoryController(CategoryService categoryService, CategoryView categoryView) {
        this.categoryService = categoryService;
        this.categoryView = categoryView;

        // 🟢 إضافة المستمع (Listener) لربط الحدث بالمنطق
        this.categoryView.getSaveButton().addActionListener(new CategorySaveListener());
    }

    // الكلاس الداخلي الذي ينفذ منطق الحفظ
    class CategorySaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String categoryName = categoryView.getCategoryName();

            if (categoryName.trim().isEmpty()) {
                categoryView.displayMessage("Error: Category name cannot be empty!", true);
                return;
            }

            try {
                // استدعاء طبقة الخدمة لتنفيذ منطق العمل
                Category newCategory = new Category(categoryName);
                Category savedCategory = categoryService.createCategory(newCategory);

                // تحديث الواجهة برسالة نجاح
                categoryView.displayMessage("Success! Created category: " + savedCategory.getName(), false);

            } catch (IllegalArgumentException ex) {
                // معالجة أخطاء الـ Validation من طبقة الخدمة
                categoryView.displayMessage("Validation Failed: " + ex.getMessage(), true);
            } catch (Exception ex) {
                // معالجة الأخطاء العامة (مثل مشاكل DB)
                categoryView.displayMessage("System Error: Check logs for details.", true);
                ex.printStackTrace();
            }
        }
    }
}