package main.java;

import main.java.dao.CategoryDAO;
import main.java.dao_impl.CategoryDAOImpl;
import main.java.service.CategoryService;
import main.java.ui.CategoryView;
import main.java.controller.CategoryController;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // 1. تجميع الـ DAO والـ Service (البنية التحتية)
        CategoryDAO categoryDAO = new CategoryDAOImpl();
        CategoryService categoryService = new CategoryService(categoryDAO);

        // 2. تشغيل الواجهة الرسومية على Thread الخاص بـ Swing (ضروري)
        SwingUtilities.invokeLater(() -> {
            // 3. إنشاء الـ View
            CategoryView categoryView = new CategoryView();

            // 4. إنشاء الـ Controller وحقن الـ Service والـ View فيه
            new CategoryController(categoryService, categoryView);
        });
    }
}