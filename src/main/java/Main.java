package main.java;

import main.java.controller.AuthController;
import main.java.controller.CategoryController;
import main.java.controller.InventoryController;
import main.java.controller.UserController;
import main.java.dao.*;
import main.java.dao_impl.*;
import main.java.service.CategoryService;
import main.java.service.OrderService;
import main.java.service.InventoryService;
import main.java.service.UserService;
import main.java.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // يجب تشغيل واجهة Swing في مسار تنفيذ الأحداث (Event Dispatch Thread)
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // ------------------ 1. تهيئة طبقة الوصول للبيانات (DAO) ------------------
        UserDAO userDAO = new UserDAOImpl();
        CategoryDAO categoryDAO = new CategoryDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();
        OrderItemDAO orderItemDAO = new OrderItemDAOImpl();

        // ------------------ 2. تهيئة طبقة منطق الأعمال (Service) ------------------
        UserService userService = new UserService(userDAO);
        CategoryService categoryService = new CategoryService(categoryDAO);
        InventoryService inventoryService = new InventoryService(productDAO, categoryService);
        OrderService orderService = new OrderService(orderDAO, orderItemDAO, productDAO, userDAO); // افتراضية

        // ------------------ 3. تهيئة طبقة التحكم (Controller) ------------------

        AuthController authController = new AuthController(userService);
        CategoryController categoryController = new CategoryController(categoryService); // إنشاء متحكم الأصناف
        InventoryController inventoryController = new InventoryController(inventoryService, categoryService);
        UserController userController = new UserController(userService);

        // يمكننا إضافة متحكم الطلبات هنا لاحقاً (OrderController)

        // ------------------ 4. بدء الواجهة الرسومية (UI) ------------------

        // تغيير دالة البناء لـ LoginFrame لتمرير CategoryController
        LoginFrame loginFrame = new LoginFrame(authController, inventoryController, categoryController, userController);
        loginFrame.setVisible(true);
    }
}