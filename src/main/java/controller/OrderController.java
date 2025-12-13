package main.java.controller;

import main.java.dao.OrderDAO;
import main.java.dao.ProductDAO;
import main.java.model.Order;
import main.java.model.OrderItem;
import main.java.model.Product;
import main.java.service.OrderService;
import main.java.util.OrderReceiptPrinter;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderController {

    private final OrderService orderService;

    // --- Constructor: Dependency Injection ---

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    // --- Data Retrieval Methods (for UI Tables) ---



    public List<Product> getAvailableProducts() {
        try {
            // من الأفضل أن تكون هناك دالة findAllProducts في OrderService
            // بدلاً من الوصول إلى DAO مباشرة، لكن سنحافظ على منطق الكود الأصلي مع تحسين معالجة الأخطاء.
            ProductDAO productDAO = orderService.getProductDAO();
            return productDAO.findAll();
        } catch (Exception e) {
            // نحول خطأ الـ SQL إلى RuntimeException (خطأ غير متوقع)
            throw new RuntimeException("Error fetching available products.", e);
        }
    }

 
    public List<Order> getAllOrders() {
        try {
            OrderDAO orderDAO = orderService.getOrderDAO();
            return orderDAO.findAll();
        } catch (Exception e) {
            // نحول خطأ الـ SQL إلى RuntimeException (خطأ غير متوقع)
            throw new RuntimeException("Error fetching order history.", e);
        }
    }

    // --- POS Action Methods ---


    public UUID placeNewOrder(UUID userId, List<OrderItem> cartItems) {
        try {
            // تفويض العملية لطبقة الخدمة التي تدير المعاملات (Transaction)
            return orderService.placeOrder(userId, cartItems);

        } catch (IllegalArgumentException e) {
            // يمسك أخطاء منطق العمل المتوقعة ويقوم بإعادة إطلاقها (Rethrow)
            throw e;
        } catch (RuntimeException e) {
            // يمسك أخطاء النظام غير المتوقعة (مثل فشل الاتصال بقاعدة البيانات)
            throw e;
        }
    }

    //to print the order
    public void printOrderReceipt(UUID orderId) {
        try {
            Optional<Order> orderOpt = orderService.findOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                List<OrderItem> items = order.getOrderItems();

                // تشغيل الطباعة في Thread منفصل
                OrderReceiptPrinter printer = new OrderReceiptPrinter(order, items);
                new Thread(printer).start();
            } else {
                throw new IllegalArgumentException("Order not found with ID: " + orderId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate printing.", e);
        }
    }

    public Optional<Order> findOrderById(UUID orderId) {
        return orderService.findOrderById(orderId);
    }
}