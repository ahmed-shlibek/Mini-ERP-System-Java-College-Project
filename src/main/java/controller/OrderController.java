package main.java.controller;

import main.java.dao.OrderDAO;
import main.java.dao.ProductDAO;
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.Product;
import main.java.service.OrderService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderController {

    private final OrderService orderService;

    // --- ProductStub: Simplified object for the UI table ---
    public static class ProductStub {
        public final UUID id;
        public final String name;
        public final long priceInCents;
        public final int stock;

        public ProductStub(UUID id, String name, long priceInCents, int stock) {
            this.id = id;
            this.name = name;
            this.priceInCents = priceInCents;
            this.stock = stock;
        }

        // Helper to get the full UUID needed for the OrderItem
        public UUID getProductId() {
            return id;
        }
    }

    // --- Constructor: Dependency Injection ---

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // -------------------------------------------------------------------
    // --- Data Retrieval Methods (for UI Tables) ---
    // -------------------------------------------------------------------

    public List<Product> getAvailableProducts() {
        try {
            // Access the DAO via the service (assuming the getter was added to OrderService)
            ProductDAO productDAO = orderService.getProductDAO();
            List<Product> products = productDAO.findAll();

            // Map the full model object (Product) to the simplified UI object (ProductStub)
            return products.stream()
                    .map(p -> new ProductStub(
                            p.getProductId(),
                            p.getName(),
                            p.getPrice(),
                            p.getQuantity()))
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            System.err.println("Error fetching available products: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Retrieves all orders for the History table by delegating to the OrderDAO.
     */
    public List<Order> getAllOrders() {
        try {
            // Access the DAO via the service (assuming the getter was added to OrderService)
            OrderDAO orderDAO = orderService.getOrderDAO();
            return orderDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Error fetching order history: " + e.getMessage());
            return List.of();
        }
    }

    // -------------------------------------------------------------------
    // --- POS Action Methods ---
    // -------------------------------------------------------------------

    /**
     * Places a new order by delegating the entire transaction (stock check,
     * stock reduction, and database inserts) to the OrderService.
     * @param userId The ID of the user placing the order.
     * @param cartItems The list of items in the current cart.
     * @return The completed Order object if successful.
     * @throws IllegalArgumentException for business logic errors (e.g., Insufficient stock).
     */
    public Order placeNewOrder(UUID userId, List<OrderItem> cartItems) {
        try {
            // 1. Delegation: Calls the service's transactional method
            UUID orderId = orderService.placeOrder(userId, cartItems);

            // 2. Fetch: Fetches the newly created Order object to return to the UI
            Optional<Order> orderOpt = orderService.findOrderById(orderId);
            return orderOpt.orElse(null);

        } catch (IllegalArgumentException e) {
            // Catches expected service errors (e.g., "Insufficient Stock," "Product not found")
            throw e;
        } catch (RuntimeException e) {
            // Catches unexpected service errors (e.g., database transaction failure)
            throw e;
        }
    }

    /**
     * Finds a specific order by ID (delegates to the Service).
     */
    public Optional<Order> findOrderById(UUID orderId) {
        return orderService.findOrderById(orderId);
    }
}