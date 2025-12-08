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
        //we get the categories first
        loadCategories();

        // 2. تهيئة الجدول
        initializeTable();

        // 3. إضافة الجدول إلى لوحة Scrolling
        //we make the table scrollable if needed
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
            JOptionPane.showMessageDialog(this, "Failed to load Categories " + e.getMessage(),
                    "Error in Data Base", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeTable() {
        // أعمدة الجدول
        String[] columnNames = {"ID", "Name", "Category", "Price", "Quantity"};
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
            JOptionPane.showMessageDialog(this,
                    "Error Loading Categories " + e.getMessage(),
                    "Data Base Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * إنشاء لوحة الأزرار (إضافة منتج، تحديث مخزون، إضافة صنف، تحديث القائمة).
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Update List");
        refreshButton.addActionListener(e -> loadProducts());
        panel.add(refreshButton);

        // الأزرار الإدارية تظهر للمدير فقط
        if ("ADMIN".equalsIgnoreCase(SessionUtil.getCurrentUser().getRole())) {

            // زر إضافة صنف جديد (حل المشكلة)
            JButton addCategoryButton = new JButton("Add New Category");
            addCategoryButton.addActionListener(e -> showAddCategoryDialog());
            panel.add(addCategoryButton);

            JButton addButton = new JButton("Add New Item");
            addButton.addActionListener(e -> showAddProductDialog());
            panel.add(addButton);

            JButton updateStockButton = new JButton("Update Inventory");
            updateStockButton.addActionListener(e -> showUpdateStockDialog());
            panel.add(updateStockButton);

            //here we created a button and when clicked on it does the deleteCategoryButton function
            //we use the action listener
            JButton deleteCategoryButton = new JButton("Delete Category");
            deleteCategoryButton.addActionListener(e -> showDeleteCategoryDialog());
            panel.add(deleteCategoryButton);

            JButton deleteButton = new JButton("Delete Item");
            deleteButton.addActionListener(e ->showDeleteDialog());
            panel.add(deleteButton);

        }

        return panel;
    }

    // ------------------ واجهات إدارة البيانات (Dialogs) ------------------

    /**
     * فتح نافذة لإضافة صنف جديد. (الحل للمشكلة)
     */
    private void showAddCategoryDialog() {
        String categoryName = JOptionPane.showInputDialog(this,
                "Enter new Category name",
                "Adding Category",
                JOptionPane.QUESTION_MESSAGE);

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            try {
                // استدعاء Controller لإنشاء الصنف
                categoryController.createCategory(categoryName.trim());
                JOptionPane.showMessageDialog(this,
                        "Category:" + categoryName + "Added Successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // تحديث قائمة الأصناف في الذاكرة ومن ثم تحديث قائمة المنتجات
                loadCategories();
                loadProducts();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to add Category: " + e.getMessage(),
                        "Error",
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
            JOptionPane.showMessageDialog(this, "Cannot add product, Please choose/add Category",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // تجميع الحقول في لوحة واحدة
        JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        dialogPanel.add(new JLabel("Product Name:"));
        dialogPanel.add(nameField);
        dialogPanel.add(new JLabel("Price(LYD/USD):"));
        dialogPanel.add(priceField);
        dialogPanel.add(new JLabel("Quantity available:"));
        dialogPanel.add(stockField);
        dialogPanel.add(new JLabel("Category"));
        dialogPanel.add(categoryComboBox);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel,
                "Adding new Category", JOptionPane.OK_CANCEL_OPTION,
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

                JOptionPane.showMessageDialog(this,
                        "Product has been added Successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts(); // تحديث الجدول

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numerical values for the price and quantity",
                        "Error in input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to add Product: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
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

    private void showDeleteCategoryDialog(){

        //we see if user is authorized
        if(!"ADMIN".equalsIgnoreCase(SessionUtil.getCurrentUser().getRole())){
            JOptionPane.showMessageDialog(this,
                    "Unathorized access only Admin can delete Category",
                    "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //this checks our categoryMap if we have any categories or naa
        if(categoryMap == null || categoryMap.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "No Categories available to Delete, Please add one first",
                    "Error",JOptionPane.ERROR_MESSAGE);
            return;
        }

        //extracts key from Map and places them into strin array for display
        String[] categoryNames = categoryMap.values().toArray(new String[0]);

        String selectedCategory = (String) JOptionPane.showInputDialog(this,
                "Select the Category to permanently Delete",
                "Delete Category",
                JOptionPane.QUESTION_MESSAGE,
                null,
                categoryNames,//List of options
                categoryNames[0]);//defualt selection

        //checks if the user cancelled the dialog
        if(selectedCategory == null){
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Deleting a category will orphan any associated products. Are you sure you want to delete'" + selectedCategory + "'?",
                "Confirm Deletion",
                //yes no option
                JOptionPane.YES_NO_OPTION,
                //warning like message
                JOptionPane.WARNING_MESSAGE);

        //after user chooses yes or no we get an int that goes to our confirm then we see if its the yes option if yes we return
        if(confirm != JOptionPane.YES_OPTION){
            return;
        }

        try{
            // this basically looks for the uuid that belongs to the name(category) we want to delete
            UUID categoryIdToDelete = null;
            for (Map.Entry<UUID, String> entry : categoryMap.entrySet()) {
                // Compare the map's VALUE (Category Name) with the selectedCategory name
                if (entry.getValue().equals(selectedCategory)) {
                    // Found a match, retrieve the KEY (UUID)
                    categoryIdToDelete = entry.getKey();
                    break;
                }
            }

            // Check if ID was found
            if (categoryIdToDelete == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: Selected category not found in Data inconsistency.",
                        "System Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Call the Controller to execute the deletion
            categoryController.deleteCategory(categoryIdToDelete);

            // Update UI
            JOptionPane.showMessageDialog(this,
                    "Category '" + selectedCategory + "' deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadCategories(); // Reload categories to update the map
            loadProducts();   // Reload products to update the table


        }catch(Exception e){
            // Handle exceptions thrown by the Service/DAO layer (e.g., integrity constraints)
            JOptionPane.showMessageDialog(this,
                    "Deletion failed: " + e.getMessage(),
                    "Deletion Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    //this will open a dialog of products we can delete from
    private void showDeleteDialog(){
        //so basically user selects row to delete - selectedrow stores that row and the producttable.... displays the row
     int selectedRow = productTable.getSelectedRow();

     //tables are zero indexed so if the selected row is -1 that means he did not select a row
     if(selectedRow == -1){
         JOptionPane.showMessageDialog(this,
                 "Please select an item from the table to delete",
                 "Selection required", JOptionPane.WARNING_MESSAGE);
         return;
     }

     //here we check the authorization , check if admin (caps not important) then we get the current user and get his role
     if(!"ADMIN".equalsIgnoreCase(SessionUtil.getCurrentUser().getRole())){
         JOptionPane.showMessageDialog(this,
                 "Unauthorized access. Only Admin can delete Products.",
                 "Permission Denied",JOptionPane.WARNING_MESSAGE);
         return;
     }

     try{
         //0 cuz our uuid is in the 0 column and 1 cuz the name is in the 1 column
         UUID productIdToDelete = (UUID) tableModel.getValueAt(selectedRow, 0);
         String productName = (String) tableModel.getValueAt(selectedRow, 1);

         //confirm deletion
         int confirm = JOptionPane.showConfirmDialog(this,
                 "Are you sure you want to permanently delete the product: '" + productName + "'?",
                 "Confirm Product Deletion",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.WARNING_MESSAGE);

         if (confirm != JOptionPane.YES_OPTION) {
             return; // User canceled deletion
         }

         inventoryController.deleteProduct(productIdToDelete);

         JOptionPane.showMessageDialog(this,
                 "Product'" + productName + "' deleted successfully.",
                 "Success", JOptionPane.INFORMATION_MESSAGE);

         loadProducts();//refresh the table
     }catch(Exception e){
         JOptionPane.showMessageDialog(this,
                 "Could not delete product do to Error"+ e.getMessage(),
                 "Deletion Failed", JOptionPane.ERROR_MESSAGE);
         }
     }

    }
