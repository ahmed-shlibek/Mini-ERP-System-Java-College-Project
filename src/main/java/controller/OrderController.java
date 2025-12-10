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

    // --- Constructor: Dependency Injection ---

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // -------------------------------------------------------------------
    // --- Data Retrieval Methods (for UI Tables) ---
    // -------------------------------------------------------------------

    /**
     * يجلب قائمة بالمنتجات المتاحة للعرض في لوحة الطلبات.
     * تم تعديل نوع الإرجاع إلى List<Product> لتجنب تعقيد ProductStub في هذه الطبقة.
     */
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

    /**
     * يجلب جميع الطلبات لعرض سجل الطلبات.
     */
    public List<Order> getAllOrders() {
        try {
            OrderDAO orderDAO = orderService.getOrderDAO();
            return orderDAO.findAll();
        } catch (Exception e) {
            // نحول خطأ الـ SQL إلى RuntimeException (خطأ غير متوقع)
            throw new RuntimeException("Error fetching order history.", e);
        }
    }

    // -------------------------------------------------------------------
    // --- POS Action Methods ---
    // -------------------------------------------------------------------

    /**
     * إنشاء طلب جديد (POS).
     * @param userId ID المستخدم الذي يقوم بإجراء الطلب.
     * @param cartItems قائمة عناصر الطلب.
     * @return ID الطلب المكتمل.
     * @throws IllegalArgumentException لأخطاء منطق العمل (مثل نقص المخزون).
     */
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

    /**
     * البحث عن طلب محدد بواسطة المعرف.
     */
    public Optional<Order> findOrderById(UUID orderId) {
        return orderService.findOrderById(orderId);
    }
}