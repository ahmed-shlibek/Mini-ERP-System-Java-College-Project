package main.java.util;

import main.java.model.Order;
import main.java.model.OrderItem;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

//this class implements the io stream and the multi thread
public class OrderReceiptPrinter implements Runnable {

    private final Order order;
    private final List<OrderItem> items;
    private final String fileName;

    public OrderReceiptPrinter(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
        // اسم الملف يتضمن ID الطلب ليكون مميزاً
        this.fileName = "receipt_" + order.getOrderId().toString().substring(0,8)+ ".txt";
    }

    @Override
    public void run() {
        System.out.println("🖨️ Printing receipt for Order: " + order.getOrderId() + " in background...");

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            // تنسيق العملة والتاريخ
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // كتابة رأس الفاتورة
            writer.println("=========================================");
            writer.println("           MINI ERP SYSTEM RECEIPT       ");
            writer.println("=========================================");
            writer.println("Order ID: " + order.getOrderId());
            writer.println("Date:     " + order.getCreatedAt().format(dtf));
            writer.println("Status:   " + order.getStatus());
            writer.println("-----------------------------------------");
            writer.println(String.format("%-20s %5s %10s", "Item", "Qty", "Price"));
            writer.println("-----------------------------------------");

            long totalCents = 0;

            // كتابة العناصر
            for (OrderItem item : items) {
                double price = item.getPriceAtOrder() / 100.0;
                long lineTotal = item.getPriceAtOrder() * item.getQuantity();
                totalCents += lineTotal;

                writer.println(String.format("%5d %10s",
                        item.getQuantity(),
                        currency.format(price)));
            }

            writer.println("-----------------------------------------");
            writer.println("TOTAL AMOUNT: " + currency.format(totalCents / 100.0));
            writer.println("=========================================");
            writer.println("       Thank you for your business!      ");

            // محاكاة عملية بطيئة (اختياري - للتوضيح فقط)
            // Thread.sleep(2000);

            System.out.println("✅ Receipt printed successfully: " + fileName);

        } catch (IOException e) {
            System.err.println("❌ Failed to print receipt: " + e.getMessage());
        }
    }
}