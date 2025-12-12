package main.java.service;

import main.java.model.Order;
import main.java.model.OrderItem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

public class ReceiptLogger {

    //constructor will create a new file called receipts_log if there isnt one if there is one it will fo the
    private static final String RECEIPT_FILE = "receipts_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logReceipt(Order order){
        try(FileWriter fw = new FileWriter(RECEIPT_FILE,true);//true enables the appened mode
            BufferedWriter bw = new BufferedWriter(fw);//wrapper and sends chunk by chunk not but by bit
            PrintWriter out = new PrintWriter(bw);//this basically helps us write the code in the file ho we want (decoration)
        ){

            //This will be our header
            out.println("=========================================");
            out.println("Receipt ID:"+order.getOrderId().toString().substring(0,8));
            out.println("Date"+order.getCreatedAt().format(FORMATTER));
            out.println("User ID:"+order.getUserId().toString().substring(0,8));
            out.println("-----------------------------------------");

            //The items
            //loop to iterate throu the items
            long totalAmount = 0;
            for(OrderItem item : order.getOrderItems()){
                long itemTotal = item.getPriceAtOrder() * item.getQuantity();
                totalAmount += itemTotal;

                out.printf("x%-3d @ $%.2f%n",//left justified 3 chars--divide by 100 then display with 2 decimal points
                        item.getQuantity(),
                        item.getPriceAtOrder() / 100.0);
            }

            //the footer
            out.println("-----------------------------------------");
            out.printf("TOTAL AMOUNT: $%.2f%n", totalAmount / 100.0);
            out.println("=========================================");
            out.println(); // Add a blank line separator


        }catch(IOException e){
            System.err.println("Error logging receipt to file:"+e.getMessage());
        }
    }

    public String extractAllReceipts(){
        StringBuilder content = new StringBuilder();//makes a string builder to hold all our strings

        //we add the buffer to allow our system to read large chunks of data from the file(receipt)
        try(java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(RECEIPT_FILE))){
            String line;
            while((line = reader.readLine()) != null)//reads line by line -- when end reached = null it will stop
            {
                content.append(line).append("\n");//appends the line we just read to our bulider (content)
            }
        }catch(IOException e){
            return "Error reading Receipt"+ e.getMessage();
        }
        return content.toString();
    }
}
