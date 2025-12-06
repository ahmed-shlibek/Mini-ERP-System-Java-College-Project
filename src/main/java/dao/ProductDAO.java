package main.java.dao;

import main.java.model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductDAO {

    //function to insert new product
    Product save(Product product)throws SQLException;

    //function to update Product
    Product update(Product product)throws SQLException;

    void updateStock(UUID productId, int quantityChange) throws SQLException;

    //deleted a product
    void delete(UUID productId)throws SQLException;

    //looks for product in the database by its id
    Optional<Product> findByProductId(UUID productId)throws SQLException;

    //looks for the product in the database by its name
    Optional<Product> findByProductName(String name)throws SQLException;

    //this returns all the products we have
    //no parameters needed cuz its gonna get everything from table
    List<Product> findAll()throws SQLException;

    //this tells us when a product is near empty
    List<Product> findLowStock(int threshold)throws SQLException;


}
