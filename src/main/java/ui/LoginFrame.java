package main.java.ui;

import main.java.controller.AuthController;
import main.java.controller.CategoryController; // استيراد متحكم الأصناف
import main.java.controller.InventoryController;
import main.java.database.DBConnection;
import main.java.model.User;
import main.java.util.SessionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * شاشة تسجيل الدخول (View Layer).
 * تتعامل فقط مع عرض المكونات واستقبال الإدخال وإرساله إلى Controller.
 */
public class LoginFrame extends JFrame {

    private final AuthController authController;
    private final InventoryController inventoryController;
    private final CategoryController categoryController; // حفظ متحكم الأصناف

    // مكونات الواجهة
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // دالة البناء تستقبل Controllers المطلوبة
    public LoginFrame(AuthController authController, InventoryController inventoryController, CategoryController categoryController) {
        this.authController = authController;
        this.inventoryController = inventoryController;
        this.categoryController = categoryController; // حقن التبعية

        // إعدادات النافذة الرئيسية
        setTitle("تسجيل الدخول - نظام إدارة الموارد");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // وضع النافذة في منتصف الشاشة

        // بناء اللوحة (Panel) التي تحتوي على حقول الإدخال
        JPanel panel = createLoginPanel();
        add(panel, BorderLayout.CENTER);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // المسافات الداخلية

        // --- حقل اسم المستخدم ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("اسم المستخدم:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // --- حقل كلمة المرور ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("كلمة المرور:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // --- زر تسجيل الدخول ---
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        loginButton = new JButton("تسجيل الدخول");
        loginButton.setPreferredSize(new Dimension(150, 30));
        loginButton.addActionListener(new LoginButtonListener());
        panel.add(loginButton, gbc);

        return panel;
    }

    /**
     * مُستمع الحدث لزر تسجيل الدخول.
     */
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (authController.login(username, password)) {
                // النجاح: إخفاء نافذة الدخول وعرض لوحة التحكم
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "تم تسجيل الدخول بنجاح! مرحباً " + SessionUtil.getCurrentUser().getUsername(),
                        "نجاح", JOptionPane.INFORMATION_MESSAGE);

                // إخفاء هذه النافذة
                dispose();

                // عرض لوحة التحكم بمرور جميع Controllers المطلوبة
                new DashboardFrame(inventoryController, categoryController).setVisible(true);

            }
        }
    }
}