package main.java.ui;

import main.java.controller.OrderController;
import main.java.dao.UserDAO;
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles.details;


public class OrderPanel extends JPanel {

    private final OrderController orderController;
    private final List<OrderItem> currentCart = new ArrayList<>();
    private List<Product> availableProducts = new ArrayList<>();

    //UI Components
    private JTable cartTable;//JTable is the view(so basically what the user sees)
    private DefaultTableModel cartTableModel;//this is the model where the data is stored
    private JTable productsTable;
    private DefaultTableModel productTableModel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JLabel totalLabel;

    public OrderPanel(OrderController controller){
        this.orderController = controller;

        //now we are gonna set up the main lay out for the panel
        setLayout(new BorderLayout(10,10));

        //now we are gonna create the horizontal split pane for POS (left side) and History (Right side)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);//this makes both sided equal

        //now we are setting up the two main views
        splitPane.setLeftComponent(createPOSPanel());
        splitPane.setRightComponent(createHistoryPanel());

        add(splitPane, BorderLayout.CENTER);

        loadProductData();
        loadOrderHistory();
    }

    //our POS (point of sale)
    private JPanel createPOSPanel(){
        JPanel posPanel = new JPanel(new BorderLayout(5,5));
        //this creates the title
        posPanel.setBorder(BorderFactory.createTitledBorder("New Order / Point Of Sale"));

        //this will split horizontally (top and bottom)
        JSplitPane posSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        posSplit.setResizeWeight(0.6);//top gets 60% of screen when resizing

        posSplit.setTopComponent(createProductSelectionPanel());
        posSplit.setBottomComponent(createCartPanel());

        //this will put our posPanel in the center
        posPanel.add(posSplit, BorderLayout.CENTER);
        return posPanel;

    }

    private JPanel createProductSelectionPanel(){
        JPanel panel= new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Products"));

        //this is our table model for out products
        String[] productsColumns = {"Product Name","Price","Quantity"};
        //productcolumn gave us the names of our columns and 0 means we start from 0
        productTableModel = new DefaultTableModel(productsColumns,0){
       // using override and this method makes us be able to click on the row but not edit it
        @Override
                public boolean isCellEditable(int row, int column) {
            return false;
        }
       };

        productsTable = new JTable(productTableModel);
        //this makes it so the user is only alloud to select one item at a time
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //button to add products to cart
        JButton addToCartButton = new JButton("Add Selected to Cart");
        //when button clicked the addtocartaction function will work
        addToCartButton.addActionListener(this::addToCartAction);

        panel.add(new JScrollPane(productsTable),BorderLayout.CENTER);
        panel.add(addToCartButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartPanel(){
        JPanel cartPanel = new JPanel(new BorderLayout(5,5));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Cart"));

        //table model for cart items
        String[] cartColumns = {"Product", "Quantity","Unit Price","Total"};
        cartTableModel = new DefaultTableModel(cartColumns,0){
            @Override
                    public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        //now this is the summary and the controls
        //southpanel is a container that holds the label and control buttons
        JPanel southPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: $0.))", SwingConstants.RIGHT);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD , 18f));

        JButton completeOrderButton = new JButton("Complete Order");
        completeOrderButton.addActionListener(this::completeOrderAction);

        JButton cancelCartButton = new JButton("Cancel Cart");
        cancelCartButton.addActionListener( e -> clearCart());

        //panel to hold the buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(cancelCartButton);
        controlPanel.add(completeOrderButton);

        southPanel.add(totalLabel, BorderLayout.NORTH);
        southPanel.add(controlPanel, BorderLayout.SOUTH);

        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(southPanel, BorderLayout.SOUTH);

        return cartPanel;
    }

    // -- our POS actions --

    //adds a selected product to the cart
    private void addToCartAction(ActionEvent e){
        int selectedRow = productsTable.getSelectedRow();
        if(selectedRow == -1 || availableProducts.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Please select a Product first.",
                    "Selection Error",JOptionPane.WARNING_MESSAGE);
            return;
        }

       Product selectedProduct = availableProducts.get(selectedRow);

        //here we are getting the quantity
        String quantityStr = JOptionPane.showInputDialog(this,
                "Enter quantity for "+ selectedProduct.getName()+ ":",
                "1");
        if(quantityStr == null) return; //user canceld

        try{
            //converts string input to numerical integer
            int quantity = Integer.parseInt(quantityStr);
            //if quantity less or equal to zero its gonna throw a exception
            if(quantity <= 0)throw new NumberFormatException();

            //cheks localy the current inventory
            if(quantity > selectedProduct.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient Stock. Only" + selectedProduct.getQuantity() + " available",
                        "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //this will check if item is already in the cart and updates the quantity
            boolean itemFound = false;
            for(OrderItem cartItem : currentCart){
                if(cartItem.getProductId().equals(selectedProduct.getProductId())){
                    //checks now the available quantity for the combined quantity
                    if(cartItem.getQuantity() + quantity > selectedProduct.getQuantity()){
                        // cartItem.getQuantity() + quantity will calculate the total wuantity in the cart
                        JOptionPane.showMessageDialog(this,
                                "Adding"+ quantity + "exceeds current stock of "+ selectedProduct.getName() ,
                                "Stock Error",JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //updates the quantity
                    cartItem.setQuantity(cartItem.getQuantity() + quantity);
                    itemFound = true;//our flag is now set indicated that the item was handeled
                    break;
                }
            }
            if(!itemFound){
                OrderItem item = new OrderItem(
                        selectedProduct.getProductId(),
                        selectedProduct.);
                currentCart.add(item);
            }
            updateCartTable();

        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,
                    "Invalid quantity enetered.",
                    "Input Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeOrderAction(ActionEvent e){
        //checks if empty
        if(currentCart.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "The cart is empty.",
                    "Order Error",JOptionPane.ERROR_MESSAGE);
            return;
        }

        UUID mockUserID = UserDAO.MOCK_USER_ID;

        try{
            UUID newOrder = orderController.placeNewOrder(mockUserID, new ArrayList<currentCart>());
            JOptionPane.showMessageDialog(this,
                    "Order"+ newOrder.toString().substring(0,8)+"placed successfully!",
                    "Success",JOptionPane.INFORMATION_MESSAGE);

            //here we refresh ui componenets to reflect changes
            clearCart();
            loadProductData();
            loadOrderHistory();
        }catch(IllegalArgumentException ex){
            //this will catch business logic error like insufficent stock
            JOptionPane.showMessageDialog(this,
                    "Order Failed -- Business Logic"+ex.getMessage(),
                    "Failure",JOptionPane.ERROR_MESSAGE);
        }catch(RuntimeException ex){
            JOptionPane.showMessageDialog(this,
                    "Order Failed -- System Error"+ex.getMessage(),
                    "Failure",JOptionPane.ERROR_MESSAGE);
        }
    }

    //this just clears the cart and updates our cart as is
    private void clearCart(){
        currentCart.clear();
        updateCartTable();
    }


    //-- Order History Panel --
    private JPanel createHistoryPanel(){
        //our container that will contain all history elements
        JPanel historyPanel = new JPanel(new BorderLayout());//use border layout so we can put in (N,W,S,E)
        historyPanel.setBorder(BorderFactory.createTitledBorder("Order History"));

        //Table Model For Order History
        String[] historyColumns = {"Order ID","Date","Total","Status"};
        historyTableModel = new DefaultTableModel(historyColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);//this shows our table on screen

        //These are the control buttons for our history
        JButton refreshButton = new JButton("Refresh History");
        // e-> is a lambda expresiion so when pressed on it does the action
        refreshButton.addActionListener(e -> loadOrderHistory);//loads our history again

        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(this::viewDetailsAction);

        // we make a container to contain our buttons to be on the right
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(refreshButton);
        controlPanel.add(viewDetailsButton);

        // we made a container that is scroallable and added all our componenets
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
        }

        String orderIdShort = (String) historyTableModel.getValueAt(selectedRow,0);

        //this basically gets us the unique id to locate the full order object
        Optional<Order> selectedOrderOpt = orderController.getAllOrders().stream()
                .filter(o -> o.getOrderId().toString().substring(0, 8).equals(orderIdShort))
                .findFirst();// the first 8 chars

        if(selectedOrderOpt.isPresent()){
            Order selectedOrder = selectedOrderOpt.get();

            StringBuilder detail = new StringBuilder(String.format("Order ID:%s\nStaff ID:%S\n" +
                    "Status:%s\nDate:%s\n\nItems:\n",
                    selectedOrder.getOrderId().toString().substring(0,8),
                    selectedOrder.getUserId().toString().substring(0,8),
                    selectedOrder.getStatus(),
                    selectedOrder.getCreatedAt()));

            for (OrderItem item : selectedOrder.get()) {
                details.append(String.format("- %s (Qty: %d) @ %s each\n",
                        item.getOrderId().toString(),
                        item.getQuantity(),
                        String.format("$%.2f", item.getPriceAtOrder() / 100.0)
                ));
            }
            JOptionPane.showMessageDialog(this,details.toString(),"Order Details",
                    JOptionPane.PLAIN_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(this,
                    "Order details could not be loaded",
                   "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProductData() {
        productTableModel.setRowCount(0);
       List<Product> availableProducts = orderController.getAvailableProducts();//we fetch from controller

        for (Product product : availableProducts){
            productTableModel.addRow(new Object[]{
                    product.getName(),
                    product.getPrice(),
                    product.getQuantity()
            });
        }
        }

        private void loadOrderHistory(){
        historyTableModel.setColumnCount(0);
        List<Order> orders = orderController.getAllOrders();

        for(Order order : orders){
            historyTableModel.addRow(new Object[]{
                    order.getOrderId().toString().substring(0,8),
                    order.getCreatedAt(),
                    order.getStatus()
            });
        }
        }

    private void updateCartTable() {
        cartTableModel.setRowCount(0); // Clear existing data
        long currentTotal = 0;

        for (OrderItem item : currentCart) {
            // Find current product info for display price (if available)
            Optional<Product> productOpt = availableProducts.stream()
                    .filter(p -> p.getProductId().equals(item.getProductId()))
                    .findFirst();

            // Use the current price from the product cache for estimated cart display
            long unitPrice = productOpt.isPresent() ? productOpt.get().getPrice() : 0;
            cartTableModel.addRow(new Object[]{
                    item.getQuantity(),
                    String.format("$%.2f", unitPrice / 100.0),
                    String.format("$%.2f", (unitPrice * item.getQuantity()) / 100.0)
            });
            currentTotal += (unitPrice * item.getQuantity());
        }

        totalLabel.setText(String.format("Total: $%.2f", currentTotal / 100.0));
    }

    }


