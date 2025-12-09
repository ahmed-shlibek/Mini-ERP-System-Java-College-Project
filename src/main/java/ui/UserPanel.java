package main.java.ui;

import main.java.controller.UserController;
import main.java.model.User;
import main.java.util.SessionUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class UserPanel extends JPanel {

    private final UserController userController;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserPanel(UserController userController) {
        this.userController = userController;
        setLayout(new BorderLayout(10, 10));

        initializeTable();

        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        add(createControlPanel(), BorderLayout.SOUTH);
        loadUsers();

    }

    private void initializeTable() {
        String[] columnNames = {"ID", "Username", "Password", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
    }


    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userController.getAllUsers();
            for (User u : users) {
                tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getPassword(),
                    u.getRole()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "User loading error: " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Update the list");
        refreshButton.addActionListener(e -> loadUsers());
        panel.add(refreshButton);

        JButton addUserButton = new JButton("Add new user");
        addUserButton.addActionListener(e -> showAddUserDialog());
        panel.add(addUserButton);

        JButton updateUserButton = new JButton("Update user");
        updateUserButton.addActionListener(e -> showUpdateUserDialog());
        panel.add(updateUserButton);

        return panel;
    }

    private void showAddUserDialog() {
        JTextField usernameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);
        String[] roles = {"ADMIN", "EMPLOYEE"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);

        JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        dialogPanel.add(new JLabel("username:"));
        dialogPanel.add(usernameField);
        dialogPanel.add(new JLabel("password:"));
        dialogPanel.add(passwordField);
        dialogPanel.add(new JLabel("role:"));
        dialogPanel.add(roleComboBox);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Add user", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();

                userController.createUser(username, password, role);
                JOptionPane.showMessageDialog(this, "The user has been added successfully.");
                loadUsers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Add-on failed: " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUpdateUserDialog() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // استخلاص البيانات الحالية من الصف المختار
            // ID موجود في العمود 0 (المخفي)
            UUID userId = (UUID) tableModel.getValueAt(selectedRow, 0);
            String currentUsername = (String) tableModel.getValueAt(selectedRow, 1);
            String currentPassword = (String) tableModel.getValueAt(selectedRow, 2);
            String currentRole = (String) tableModel.getValueAt(selectedRow, 3);

            // واجهة التعديل
            JTextField usernameField = new JTextField(currentUsername, 10);
            JPasswordField passwordField = new JPasswordField(10); // حقل كلمة مرور فارغ للتغيير الاختياري
            String[] roles = {"ADMIN", "EMPLOYEE"};
            JComboBox<String> roleComboBox = new JComboBox<>(roles);
            roleComboBox.setSelectedItem(currentRole);

            JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            dialogPanel.add(new JLabel("ID (cannot be changed):"));
            dialogPanel.add(new JLabel(userId.toString()));
            dialogPanel.add(new JLabel("New username:"));
            dialogPanel.add(usernameField);
            dialogPanel.add(new JLabel("The role of the user you want to update:"));
            dialogPanel.add(roleComboBox);
            dialogPanel.add(new JLabel("The password of the user you want to update:"));
            dialogPanel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(this, dialogPanel,
                    "Edit user data: " + currentUsername,
                    JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String newUsername = usernameField.getText();
                String newPassword = new String(passwordField.getPassword());
                String newRole = (String) roleComboBox.getSelectedItem();

                // 1. إنشاء كائن المستخدم المحدث
                // نستخدم الكائن الحالي ونعدل الخصائص التي سمحنا بتغييرها
                User updatedUser = new User(userId, newUsername, null, newRole);

                // 2. معالجة كلمة المرور
                // إذا أدخل المشرف كلمة مرور جديدة، نمررها للتحديث، وإلا نتركها فارغة في الدالة
                // ملاحظة: يجب تعديل دالة updateUser في UserController لتقبل كلمة مرور اختيارية
                String finalPassword = newPassword.isEmpty() ? currentPassword : newPassword;
                updatedUser.setPassword(finalPassword);

                // 3. استدعاء Controller (يجب عليك إنشاء هذه الدالة في UserController)
                userController.updateUser(updatedUser);

                JOptionPane.showMessageDialog(this, "The user was successfully updated.", "success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers(); // تحديث الجدول
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "User modification failed: " + e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        }
    }
}