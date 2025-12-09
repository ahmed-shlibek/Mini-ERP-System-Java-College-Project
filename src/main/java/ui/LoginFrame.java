package main.java.ui;

import main.java.controller.*;
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
    private final UserController userController;
    private final OrderController orderController;

    // مكونات الواجهة
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // دالة البناء تستقبل Controllers المطلوبة
    //our constructor
    public LoginFrame(AuthController authController, InventoryController inventoryController, CategoryController categoryController, UserController userController, OrderController orderController) {
        this.authController = authController;
        this.inventoryController = inventoryController;
        this.categoryController = categoryController; // حقن التبعية
        this.userController = userController;
        this.orderController = orderController;

        // إعدادات النافذة الرئيسية
        //we have these default settings for our pannel
        setTitle("Mini ERP System Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // here we use null to have the pannel in the middle of the screen
        setLocationRelativeTo(null);

        // بناء اللوحة (Panel) التي تحتوي على حقول الإدخال
        JPanel panel = createLoginPanel();
        add(panel, BorderLayout.CENTER);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        //we use the gbc to help us organize the different sections and fields in our pannel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // المسافات الداخلية

        //here we have the username label
        //gridx represents the column index and gridy represents the rows
        gbc.gridx = 0;
        gbc.gridy = 0;
        //gbc.anchor helps us choose where to display our label
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        //makes the field horizontal
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //input field 15 long
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // --- حقل كلمة المرور ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // --- زر تسجيل الدخول ---
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        loginButton = new JButton("Login");
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
                        "Welcome " + SessionUtil.getCurrentUser().getUsername(),
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                // إخفاء هذه النافذة
                dispose();

                // عرض لوحة التحكم بمرور جميع Controllers المطلوبة
                new DashboardFrame(inventoryController, categoryController, userController, orderController).setVisible(true);

            }else{
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Invalid Username or Password",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);

                //here we cleared the password text for security reasons
                passwordField.setText("");
            }
        }
    }
}