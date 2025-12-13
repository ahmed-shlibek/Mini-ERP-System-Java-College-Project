package main.java.ui;

import main.java.controller.InventoryController;
import main.java.controller.OrderController;
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.Product;
import main.java.util.SessionUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class OrderPanel extends JPanel {

    private final OrderController orderController;
    private final InventoryController inventoryController;
    private final List<OrderItem> currentCart = new ArrayList<>();
    // تم تغيير هذا المتغير ليتم تعيينه في loadProductData()
    private List<Product> availableProducts = new ArrayList<>();

    //UI Components
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JTable productsTable;
    private DefaultTableModel productTableModel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JLabel totalLabel;

    // تنسيق الأرقام لعرض العملة
    private static final java.text.NumberFormat CURRENCY_FORMAT =
            java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "US"));


    public OrderPanel(OrderController orderController, InventoryController inventoryController) {
        this.orderController = orderController;
        this.inventoryController = inventoryController;

        // الآن نقوم بإعداد التخطيط الرئيسي للوحة (Panel)
        setLayout(new BorderLayout(10,10));

        // إنشاء لوحة تقسيم أفقية (POS (اليسار) والسجل (اليمين))
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5); // لجعل الجانبين متساويين في الحجم

        // إعداد العرضين الرئيسيين
        splitPane.setLeftComponent(createPOSPanel());
        splitPane.setRightComponent(createHistoryPanel());

        add(splitPane, BorderLayout.CENTER);

        // التحميل الأولي للبيانات
        loadProductData();
        loadOrderHistory();
    }

    // لوحة نقطة البيع (POS)
    private JPanel createPOSPanel(){
        JPanel posPanel = new JPanel(new BorderLayout(5,5));
        posPanel.setBorder(BorderFactory.createTitledBorder("New Order / Point Of Sale"));

        // تقسيم عمودي (الأعلى للمنتجات، والأسفل للعربة)
        JSplitPane posSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        posSplit.setResizeWeight(0.6); // 60% للجانب العلوي

        posSplit.setTopComponent(createProductSelectionPanel());
        posSplit.setBottomComponent(createCartPanel());

        posPanel.add(posSplit, BorderLayout.CENTER);
        return posPanel;

    }

    private JPanel createProductSelectionPanel(){
        JPanel panel= new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Products"));

        String[] productsColumns = {"Product Name","Price","Quantity"};
        productTableModel = new DefaultTableModel(productsColumns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productsTable = new JTable(productTableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addToCartButton = new JButton("Add Selected to Cart");
        addToCartButton.addActionListener(this::addToCartAction);

        panel.add(new JScrollPane(productsTable),BorderLayout.CENTER);
        panel.add(addToCartButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartPanel(){
        JPanel cartPanel = new JPanel(new BorderLayout(5,5));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Cart"));

        String[] cartColumns = {"Product", "Quantity","Unit Price","Total"};
        cartTableModel = new DefaultTableModel(cartColumns,0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);

        JPanel southPanel = new JPanel(new BorderLayout());

        // تم تصحيح تنسيق النص
        totalLabel = new JLabel("Total: " + CURRENCY_FORMAT.format(0), SwingConstants.RIGHT);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD , 18f));

        JButton completeOrderButton = new JButton("Complete Order");
        completeOrderButton.addActionListener(this::completeOrderAction);

        JButton cancelCartButton = new JButton("Cancel Cart");
        cancelCartButton.addActionListener( e -> clearCart());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(cancelCartButton);
        controlPanel.add(completeOrderButton);

        southPanel.add(totalLabel, BorderLayout.NORTH);
        southPanel.add(controlPanel, BorderLayout.SOUTH);

        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(southPanel, BorderLayout.SOUTH);

        return cartPanel;
    }

    // -- POS actions --

    private void addToCartAction(ActionEvent e){
        int selectedRow = productsTable.getSelectedRow();
        if(selectedRow == -1 || availableProducts.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Please select a Product first.",
                    "Selection Error",JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = availableProducts.get(selectedRow);

        String quantityStr = JOptionPane.showInputDialog(this,
                "Enter quantity for "+ selectedProduct.getName()+ ":",
                "1");
        if(quantityStr == null) return; // user cancelled

        try{
            int quantity = Integer.parseInt(quantityStr);
            if(quantity <= 0)throw new NumberFormatException();

            if(quantity > selectedProduct.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient Stock. Only " + selectedProduct.getQuantity() + " available",
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean itemFound = false;
            for(OrderItem cartItem : currentCart){
                if(cartItem.getProductId().equals(selectedProduct.getProductId())){
                    // التحقق من الكمية المتاحة للكمية المجمعة
                    if(cartItem.getQuantity() + quantity > selectedProduct.getQuantity()){
                        JOptionPane.showMessageDialog(this,
                                "Adding "+ quantity + " exceeds current stock of "+ selectedProduct.getName() ,
                                "Stock Error",JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    cartItem.setQuantity(cartItem.getQuantity() + quantity);
                    itemFound = true;
                    break;
                }
            }
            if(!itemFound){
                // OrderItem يحتاج الآن إلى سعر المنتج الحالي
                OrderItem item = new OrderItem(
                        null,
                        selectedProduct.getProductId(),
                        selectedProduct.getPrice(), // إضافة سعر المنتج الحالي
                        quantity);
                currentCart.add(item);
            }
            updateCartTable();

        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,
                    "Invalid quantity entered.",
                    "Input Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeOrderAction(ActionEvent e){
        if(currentCart.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "The cart is empty.",
                    "Order Error",JOptionPane.ERROR_MESSAGE);
            return;
        }


        try{
            // تم تصحيح خطأ تمرير القائمة
            UUID newOrderId = orderController.placeNewOrder(SessionUtil.getCurrentUser().getUserId(), currentCart);
            JOptionPane.showMessageDialog(this,
                    "Order "+ newOrderId.toString().substring(0,8)+" placed successfully!",
                    "Success",JOptionPane.INFORMATION_MESSAGE);

            // تحديث مكونات الواجهة
            clearCart();
            loadProductData();
            loadOrderHistory();
        }catch(IllegalArgumentException ex){
            // منطق العمل (نقص المخزون، الخ)
            JOptionPane.showMessageDialog(this,
                    "Order Failed -- Business Logic: "+ex.getMessage(),
                    "Failure",JOptionPane.ERROR_MESSAGE);
        }catch(RuntimeException ex){
            // خطأ في النظام
            JOptionPane.showMessageDialog(this,
                    "Order Failed -- System Error: "+ex.getMessage(),
                    "Failure",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCart(){
        currentCart.clear();
        updateCartTable();
    }


    //-- Order History Panel --
    private JPanel createHistoryPanel(){
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Order History"));

        String[] historyColumns = {"Order ID","Date","Total","Status"};
        historyTableModel = new DefaultTableModel(historyColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);

        JButton refreshButton = new JButton("Refresh History");
        // تم تصحيح استدعاء الدالة
        refreshButton.addActionListener(e -> loadOrderHistory());

        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(this::viewDetailsAction);


        JButton printButton = new JButton("Print Receipt");
        printButton.addActionListener(this::printOrderAction);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(refreshButton);
        controlPanel.add(viewDetailsButton);
        controlPanel.add(printButton);

        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        historyPanel.add(controlPanel, BorderLayout.SOUTH);

        return historyPanel;
    }

    //methods

    private void viewDetailsAction(ActionEvent e){
        int selectedRow = historyTable.getSelectedRow();
        if(selectedRow == -1){
            JOptionPane.showMessageDialog(this,
                    "Please select an Order from the history table.",
                    "Selection Error",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderIdShort = (String) historyTableModel.getValueAt(selectedRow,0);

        // تم استبدال getAllOrders() بـ findOrderById() إذا كانت متوفرة أو بقائمة الطلبات المحملة
        // هنا نعتمد على جلب القائمة الكاملة أولاً ثم البحث فيها
        Optional<Order> selectedOrderOpt = orderController.getAllOrders().stream()
                .filter(o -> o.getOrderId().toString().substring(0, 8).equals(orderIdShort))
                .findFirst();

        if(selectedOrderOpt.isPresent()){
            Order selectedOrder = selectedOrderOpt.get();

            // تم تصحيح StringBuilder من details إلى detail
            StringBuilder detail = new StringBuilder(String.format("Order ID: %s\nStaff ID: %s\n" +
                            "Status: %s\nDate: %s\n\nItems:\n",
                    selectedOrder.getOrderId().toString().substring(0,8),
                    // يفترض وجود دالة getUserId
                    selectedOrder.getUserId() != null ? selectedOrder.getUserId().toString().substring(0,8) : "N/A",
                    selectedOrder.getStatus(),
                    selectedOrder.getCreatedAt()));

            // تم تصحيح الدالة selectedOrder.get() إلى selectedOrder.getOrderItems() (بافتراض هذا الاسم)
            // ويجب التأكد من وجود هذه الدالة في كلاس Order
            List<OrderItem> items = selectedOrder.getOrderItems();

            for (OrderItem item : items) {
                // يفترض أن item.getProductId() هو اسم المنتج هنا في OrderItem
                detail.append(String.format("- %s (Qty: %d) @ %s each\n",
                        item.getProductId(), // يفترض وجود getName() في OrderItem
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(item.getPriceAtOrder() / 100.0) // تنسيق العملة
                ));
            }
            JOptionPane.showMessageDialog(this,detail.toString(),"Order Details",
                    JOptionPane.PLAIN_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(this,
                    "Order details could not be loaded",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadProductData() {
        productTableModel.setRowCount(0);
        // تم تصحيح الخطأ: يتم التخزين في متغير الكلاس availableProducts
        this.availableProducts = inventoryController.getAllProducts();

        for (Product product : availableProducts){
            // يفترض أن PriceInCents يتم تحويله للعرض
            double priceInDollars = product.getPrice() / 100.0;
            productTableModel.addRow(new Object[]{
                    product.getName(),
                    CURRENCY_FORMAT.format(priceInDollars),
                    product.getQuantity()
            });
        }
    }



    private void loadOrderHistory(){
        // تم تصحيح الخطأ: مسح الصفوف بدلاً من الأعمدة
        historyTableModel.setRowCount(0);
        List<Order> orders = orderController.getAllOrders();

        // يجب أن نجد طريقة لجلب المجموع الكلي لكل طلب هنا إذا لم يكن موجوداً في كائن Order
        for(Order order : orders){
            // يفترض أن Total يتم حسابه أو تخزينه في كائن Order
            double totalInDollars = order.getTotal() / 100.0;
            historyTableModel.addRow(new Object[]{
                    order.getOrderId().toString().substring(0,8),
                    order.getCreatedAt(),
                    CURRENCY_FORMAT.format(totalInDollars), // يجب أن يحتوي كائن Order على دالة getTotal
                    order.getStatus()
            });
        }
    }


    private void updateCartTable() {
        cartTableModel.setRowCount(0); // مسح جميع الصفوف

        long grandTotalCents = 0;

        for (OrderItem item : currentCart) {
            long totalForItemCents = item.getQuantity() * item.getPriceAtOrder();
            grandTotalCents += totalForItemCents;

            double unitPriceDollars = item.getPriceAtOrder() / 100.0;
            double totalDollars = totalForItemCents / 100.0;

            cartTableModel.addRow(new Object[]{
                    item.getProductId(), // يفترض وجود getName() في OrderItem
                    item.getQuantity(),
                    CURRENCY_FORMAT.format(unitPriceDollars),
                    CURRENCY_FORMAT.format(totalDollars)
            });
        }

        // تحديث إجمالي الفاتورة
        totalLabel.setText("Total: " + CURRENCY_FORMAT.format(grandTotalCents / 100.0));
    }


    // ✅ دالة التعامل مع حدث الطباعة
    private void printOrderAction(ActionEvent e) {
        int selectedRow = historyTable.getSelectedRow();
        if(selectedRow == -1){
            JOptionPane.showMessageDialog(this,
                    "Please select an Order to print.",
                    "Selection Error",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderIdShort = (String) historyTableModel.getValueAt(selectedRow,0);

        // البحث عن الطلب الكامل باستخدام المعرف المختصر (نفس منطق viewDetails)
        Optional<Order> selectedOrderOpt = orderController.getAllOrders().stream()
                .filter(o -> o.getOrderId().toString().substring(0, 8).equals(orderIdShort))
                .findFirst();

        if (selectedOrderOpt.isPresent()) {
            UUID orderId = selectedOrderOpt.get().getOrderId();

            // استدعاء الكونترولر للطباعة
            try {
                orderController.printOrderReceipt(orderId);
                JOptionPane.showMessageDialog(this,
                        "Receipt printing started in background...",
                        "Printing", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to print: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}