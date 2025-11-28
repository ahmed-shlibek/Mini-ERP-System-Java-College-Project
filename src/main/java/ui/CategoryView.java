package main.java.ui; // حزمة جديدة لواجهة المستخدم (User Interface)

import javax.swing.*;
import java.awt.*;

public class CategoryView extends JFrame {

    // المكونات
    private JTextField categoryNameField;
    private JButton saveButton;
    private JTextArea messageArea;

    public CategoryView() {
        super("Category Management"); // عنوان النافذة

        // إعداد الواجهة
        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // بناء اللوحات والمكونات
        JPanel inputPanel = new JPanel(new FlowLayout());
        categoryNameField = new JTextField(20);
        saveButton = new JButton("Save Category");
        messageArea = new JTextArea(5, 30);
        messageArea.setEditable(false);

        inputPanel.add(new JLabel("Category Name:"));
        inputPanel.add(categoryNameField);
        inputPanel.add(saveButton);

        // إضافة المكونات للنافذة
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        setVisible(true);
    }

    // دوال getter للوصول إلى المكونات
    public JButton getSaveButton() {
        return saveButton;
    }

    public String getCategoryName() {
        return categoryNameField.getText();
    }

    public void displayMessage(String message, boolean isError) {
        messageArea.append(message + "\n");
        // يمكن تغيير لون النص هنا للإشارة إلى خطأ
        if (isError) {
            messageArea.setForeground(Color.RED);
        } else {
            messageArea.setForeground(Color.BLACK);
        }
    }
}