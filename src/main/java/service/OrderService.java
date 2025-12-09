package main.java.service;

import main.java.dao.OrderDAO;
import main.java.dao.OrderItemDAO;
import main.java.dao.ProductDAO; // نحتاج ProductDAO للتحقق من المخزون وتحديثه
import main.java.dao.UserDAO;     // نحتاج UserDAO للتحقق من وجود المستخدم
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.Product;
import main.java.model.User;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;

    public OrderService(OrderDAO orderDAO, OrderItemDAO orderItemDAO, ProductDAO productDAO, UserDAO userDAO) {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
    }

    public Optional<Order> findOrderById(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null.");
        }
        try {
            return orderDAO.findById(orderId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while fetching order by ID: " + orderId, e);
        }
    }

    public void deleteOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null for deletion.");
        }

        try {
            if (orderDAO.findById(orderId).isEmpty()) {
                throw new IllegalArgumentException("Order with ID " + orderId + " not found. Cannot delete.");
            }

            orderItemDAO.deleteByOrderId(orderId);

            orderDAO.delete(orderId);

        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred during order deletion for ID: " + orderId, e);
        }
    }

    public UUID placeOrder(UUID userId, List<OrderItem> cartItems) {
        if (userId == null || cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("User ID and Cart Items must be provided to place an order.");
        }

        try {
            Optional<User> userOpt = userDAO.findUserById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("User with ID " + userId + " not found.");
            }

            for (OrderItem item : cartItems) {
                Optional<Product> productOpt = productDAO.findByProductId(item.getProductId());

                if (productOpt.isEmpty()) {
                    throw new IllegalArgumentException("Product ID " + item.getProductId() + " not found.");
                }

                Product product = productOpt.get();

                if (product.getQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                            ". Available: " + product.getQuantity() +
                            ", Requested: " + item.getQuantity());
                }

                item.setPriceAtOrder(product.getPrice());
                item.setOrderId(UUID.randomUUID());
            }

            Order newOrder = new Order(null, userId, "PENDING", LocalDateTime.now());
            newOrder = orderDAO.save(newOrder);
            UUID finalOrderId = newOrder.getOrderId();

            for (OrderItem item : cartItems) {
                item.setOrderId(finalOrderId);
            }

            orderItemDAO.saveAll(cartItems);

            for (OrderItem item : cartItems) {
                productDAO.updateStock(item.getProductId(), item.getQuantity() * -1);
            }

            newOrder.setStatus("PROCESSING");
            orderDAO.update(newOrder);

            return finalOrderId;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to complete order transaction due to database error.", e);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    //added the two getters
    public ProductDAO getProductDAO() {
        return productDAO;
    }
    public OrderDAO getOrderDAO() {
        return orderDAO;
    }
}