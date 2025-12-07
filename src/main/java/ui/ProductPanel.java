package main.java.ui;

import main.java.controller.CategoryController; // استيراد متحكم الأصناف
import main.java.controller.InventoryController;
import main.java.model.Category;
import main.java.model.Product;
import main.java.util.SessionUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * لوحة خاصة لعرض وإدارة المنتجات (Add/Update Stock).
 */
public class ProductPanel extends JPanel {

    private final InventoryController inventoryController;
    private final CategoryController categoryController; // حفظ متحكم الأصناف
    private JTable productTable;
    private DefaultTableModel tableModel;

    // لربط اسم الصنف بالـ UUID
    private Map<UUID, String> categoryMap = new HashMap<>();

    public ProductPanel(InventoryController inventoryController, CategoryController categoryController) {
        this.inventoryController = inventoryController;
        this.categoryController = categoryController; // حقن التبعية

        setLayout(new BorderLayout(10, 10)); // تنسيق إطار المنتج

        // 1. جلب الأصناف أولاً
        loadCategories();

        // 2. تهيئة الجدول
        initializeTable();

        // 3. إضافة الجدول إلى لوحة Scrolling
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // 4. إضافة لوحة التحكم بالأزرار
        add(createControlPanel(), BorderLayout.SOUTH);

        // تحميل البيانات الأولية
        loadProducts();
    }

    /**
     * جلب الأصناف من Controller وتخزينها في خريطة (Map) للعرض.
     */
    private void loadCategories() {
        try {
            List<Category> categories = categoryController.getAllCategories(); // استخدام CategoryController
            categoryMap.clear();
            for (Category c : categories) {
                categoryMap.put(c.getCategoryId(), c.getName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في تحميل الأصناف: " + e.getMessage(),
                    "خطأ قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeTable() {
        // أعمدة الجدول
        String[] columnNames = {"ID", "الاسم", "الصنف", "السعر", "المخزون"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // جعل جميع الخلايا غير قابلة للتعديل مباشرة
                return false;
            }
        };
        productTable = new JTable(tableModel);
    }

    /**
     * جلب المنتجات من Controller وتحميلها في الجدول.
     */
    private void loadProducts() {
        // مسح البيانات القديمة
        tableModel.setRowCount(0);
        try {
            List<Product> products = inventoryController.getAllProducts();
            for (Product p : products) {
                // يتم تحويل السعر من Long (مثلاً سنتات) إلى double (عملة)
                double price = p.getPrice() / 100.0;

                // استخدام الخريطة لتحويل UUID الصنف إلى اسم الصنف
                String categoryName = categoryMap.getOrDefault(p.getCategoryId(), "Unknown");

                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getName(),
                        categoryName,
                        String.format("%.2f", price),
                        p.getQuantity()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ في تحميل المنتجات: " + e.getMessage(),
                    "خطأ قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * إنشاء لوحة الأزرار (إضافة منتج، تحديث مخزون، إضافة صنف، تحديث القائمة).
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("تحديث القائمة");
        refreshButton.addActionListener(e -> loadProducts());
        panel.add(refreshButton);

        // الأزرار الإدارية تظهر للمدير فقط
        if ("ADMIN".equalsIgnoreCase(SessionUtil.getCurrentUser().getRole())) {

            // زر إضافة صنف جديد (حل المشكلة)
            JButton addCategoryButton = new JButton("إضافة صنف جديد");
            addCategoryButton.addActionListener(e -> showAddCategoryDialog());
            panel.add(addCategoryButton);

            JButton addButton = new JButton("إضافة منتج جديد");
            addButton.addActionListener(e -> showAddProductDialog());
            panel.add(addButton);

            JButton updateStockButton = new JButton("تحديث المخزون");
            updateStockButton.addActionListener(e -> showUpdateStockDialog());
            panel.add(updateStockButton);
        }

        return panel;
    }

    // ------------------ واجهات إدارة البيانات (Dialogs) ------------------

    /**
     * فتح نافذة لإضافة صنف جديد. (الحل للمشكلة)
     */
    private void showAddCategoryDialog() {
        String categoryName = JOptionPane.showInputDialog(this,
                "أدخل اسم الصنف الجديد:",
                "إضافة صنف",
                JOptionPane.QUESTION_MESSAGE);

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            try {
                // استدعاء Controller لإنشاء الصنف
                categoryController.createCategory(categoryName.trim());
                JOptionPane.showMessageDialog(this,
                        "تم إضافة الصنف: " + categoryName + " بنجاح.",
                        "نجاح",
                        JOptionPane.INFORMATION_MESSAGE);

                // تحديث قائمة الأصناف في الذاكرة ومن ثم تحديث قائمة المنتجات
                loadCategories();
                loadProducts();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "فشل إضافة الصنف: " + e.getMessage(),
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * فتح نافذة لإضافة منتج جديد. (تم التنفيذ الآن)
     */
    private void showAddProductDialog() {
        // حقول الإدخال
        JTextField nameField = new JTextField(10);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(10);

        // إعادة تحميل الأصناف لضمان التحديث قبل العرض
        loadCategories();

        // قائمة الأصناف المنسدلة
        JComboBox<String> categoryComboBox = new JComboBox<>(categoryMap.values().toArray(new String[0]));

        // التأكد من وجود أصناف
        if (categoryMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لا يمكن إضافة منتج. الرجاء إضافة أصناف أولاً.",
                    "خطأ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // تجميع الحقول في لوحة واحدة
        JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        dialogPanel.add(new JLabel("اسم المنتج:"));
        dialogPanel.add(nameField);
        dialogPanel.add(new JLabel("السعر (بالدينار/العملة):"));
        dialogPanel.add(priceField);
        dialogPanel.add(new JLabel("الكمية المتوفرة:"));
        dialogPanel.add(stockField);
        dialogPanel.add(new JLabel("الصنف:"));
        dialogPanel.add(categoryComboBox);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel,
                "إضافة منتج جديد", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // 1. استخلاص البيانات
                String name = nameField.getText();
                double priceDouble = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String selectedCategoryName = (String) categoryComboBox.getSelectedItem();

                // 2. تحويل السعر إلى سنتات (Long)
                long priceInCents = (long) (priceDouble * 100);

                // 3. البحث عن UUID الصنف
                UUID categoryId = null;
                for (Map.Entry<UUID, String> entry : categoryMap.entrySet()) {
                    if (entry.getValue().equals(selectedCategoryName)) {
                        categoryId = entry.getKey();
                        break;
                    }
                }

                if (categoryId == null) {
                    throw new IllegalArgumentException("Category ID not found.");
                }

                // 4. استدعاء Controller لإضافة المنتج
                inventoryController.createProduct(name, priceInCents, stock, categoryId);

                JOptionPane.showMessageDialog(this, "تم إضافة المنتج بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                loadProducts(); // تحديث الجدول

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "الرجاء إدخال قيم رقمية صحيحة للسعر والكمية.",
                        "خطأ في الإدخال", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "فشل إضافة المنتج: " + e.getMessage(),
                        "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * فتح نافذة لتحديث كمية المخزون لمنتج موجود.
     */
    private void showUpdateStockDialog() {
        // يتم طلب ID المنتج والكمية الجديدة
        JOptionPane.showMessageDialog(this, "سيتم تنفيذ نافذة تحديث المخزون هنا.",
                "قيد التنفيذ", JOptionPane.INFORMATION_MESSAGE);
        // بعد التنفيذ الناجح: loadProducts();
    }
}