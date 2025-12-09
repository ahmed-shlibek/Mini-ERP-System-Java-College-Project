package main.java.ui;

import main.java.controller.CategoryController; // استيراد متحكم الأصناف
import main.java.controller.InventoryController;
import main.java.controller.OrderController;
import main.java.controller.UserController;
import main.java.util.SessionUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * لوحة التحكم الرئيسية.
 * تستخدم JTabbedPane للتنقل بين شاشات إدارة النظام.
 */
public class DashboardFrame extends JFrame {

    private final InventoryController inventoryController;
    private final CategoryController categoryController; // حفظ متحكم الأصناف
    private final UserController userController;
    private final OrderController orderController;

    public DashboardFrame(InventoryController inventoryController, CategoryController categoryController,  UserController userController,  OrderController orderController) {
        this.inventoryController = inventoryController;
        this.categoryController = categoryController; // حقن التبعية
        this.userController = userController;
        this.orderController = orderController;

        // إعدادات النافذة
        setTitle("Main Dash Board - Welcome" + SessionUtil.getCurrentUser().getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // نتحكم في الإغلاق لعمل Logout
        setLocationRelativeTo(null);

        // تأكيد تسجيل الخروج عند الإغلاق
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(DashboardFrame.this,
                        "Are you sure you want to Log Out?",
                        "Log Out", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    SessionUtil.logout();
                    System.exit(0);
                }
            }
        });

        // إنشاء لوحة التبويبات (Tabs)
        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. لوحة المنتجات (تمرير CategoryController)
        ProductPanel productPanel = new ProductPanel(inventoryController, categoryController);
        tabbedPane.addTab("Product Management", productPanel);

        // 2. لوحة الطلبات (وهمية حالياً)
        OrderPanel orderPanel = new OrderPanel(orderController, inventoryController);
        tabbedPane.addTab("Order Management", orderPanel);

        // 3. لوحة المستخدمين (للأدمين فقط - وهمية حالياً)
        if ("ADMIN".equalsIgnoreCase(SessionUtil.getCurrentUser().getRole())) {
            UserPanel userPanel = new UserPanel(userController);
            tabbedPane.addTab("User Management", userPanel);
        }

        add(tabbedPane, BorderLayout.CENTER);
    }
}