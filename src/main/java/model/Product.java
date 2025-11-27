package main.java.model;

import java.util.UUID;

public class Product {

    private UUID productId;
    private String name;
    private UUID categoryId;
    private  long price;
    private int quantity;

    public Product(){

    }
    public Product(String name , UUID category , long price, int quantity){

        this.productId = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    //Getters
    public UUID getProductId(){
        return productId;
    }

    public String getName(){
        return name;
    }

    public UUID getCategoryId(){
        return categoryId;
    }

    public long getPrice(){
        return price;
    }

    public int getQuantity(){
        return quantity;
    }

    //Setters
    public void setProductId(UUID productId){
        this.productId = productId;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setCategoryId(UUID categoryId){
        this.categoryId = categoryId;
    }

    public void setPrice(long price){
        this.price = price;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }


    @Override
    public String toString(){
        return "Product info : {"+
        "Product ID :"+ productId +
        "Product Name :"+ name +
        "Product Category :"+ categoryId +
        "Product Price :" + price +
        "product quantity :"+ quantity + "}";
    }
}
