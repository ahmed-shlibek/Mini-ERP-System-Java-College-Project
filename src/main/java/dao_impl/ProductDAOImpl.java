package main.java.dao_impl;

import main.java.dao.ProductDAO;
import main.java.database.DBConnection;
import main.java.model.Product;
import main.java.util.DaoUtil;
import main.java.util.ResultSetMapper;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductDAOImpl implements ProductDAO, ResultSetMapper<Product> {

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (product_id , name ,category_id, price,quantity ) VALUES (?,?,?,?,?)";
    private static final String UPDATE_PRODUCT_SQL = "UPDATE products SET name = ?, category_id = ?, price = ? ,quantity =? WHERE product_id= ?";
    private static final String DELETE_PRODUCT_SQL = "DELETE FROM products where product_id = ?";
    private static final String FIND_BY_PRODUCTID_SQL ="SELECT * FROM products WHERE product_id =?";
    private static final String FIND_BY_PRODUCTNAME_SQL = "SELECT * FROM products WHERE name=?";
    private static final String FIND_ALL_SQL = "SELECT * FROM products";
    private static final String FIND_LOW_STOCK_SQL = "SELECT * FROM products WHERE quantity < ?";
    private static final String UPDATE_STOCK_SQL = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";

    @Override
    public Product map(ResultSet rs) throws SQLException {
        byte[] pidBytes = rs.getBytes("product_id");
        UUID productId = DaoUtil.bytesToUUID(pidBytes);

        byte[] cidBytes = rs.getBytes("category_id");
        UUID categoryId = DaoUtil.bytesToUUID(cidBytes);

        String name = rs.getString("name");
        long price = rs.getLong("price");
        int quantity = rs.getInt("quantity");

        Product product = new Product();
        product.setProductId(productId);
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setPrice(price);
        product.setQuantity(quantity);

        return product;
    }

    @Override
    public Product save(Product product) throws SQLException {
        if(product.getProductId() == null){
            product.setProductId(UUID.randomUUID());
        }
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(INSERT_PRODUCT_SQL)) {

            byte[] productIdBytes = DaoUtil.uuidToBytes(product.getProductId());
            byte[] categoryIdBytes = DaoUtil.uuidToBytes(product.getCategoryId());

            ps.setBytes(1,productIdBytes);
            ps.setString(2,product.getName());
            ps.setBytes(3,categoryIdBytes);
            ps.setLong(4,product.getPrice());
            ps.setInt(5,product.getQuantity());

            ps.executeUpdate();

        }

        return product;
    }

    @Override
    public Product update(Product product)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(UPDATE_PRODUCT_SQL)){

            ps.setString(1,product.getName());
            ps.setBytes(2,DaoUtil.uuidToBytes(product.getCategoryId()));
            ps.setLong(3,product.getPrice());
            ps.setInt(4,product.getQuantity());
            ps.setBytes(5,DaoUtil.uuidToBytes(product.getProductId()));

            ps.executeUpdate();
        }

        return product;
    }

    @Override
    public void updateStock(UUID productId, int quantityChange) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STOCK_SQL)) {

            stmt.setInt(1, quantityChange);

            stmt.setBytes(2, DaoUtil.uuidToBytes(productId));

            stmt.executeUpdate();

        }
    }

    @Override
    public void delete(UUID productId)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(DELETE_PRODUCT_SQL)){

            ps.setBytes(1,DaoUtil.uuidToBytes(productId));
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Product> findByProductId(UUID productId)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(FIND_BY_PRODUCTID_SQL)){

            ps.setBytes(1,DaoUtil.uuidToBytes(productId));

            try(ResultSet rs = ps.executeQuery()){

                if(rs.next()){
                    //the mapper function translates our data into objects (basically a language that java
                    //understands it
                    Product product = map(rs);

                    //we use optional cuz it might return and might not so basically optional
                    return Optional.of(product);

                    // if user found we return user if not we return an empty container
                    //the query worked but did not find the product
                }return Optional.empty();
            }
        }
    }


    @Override
    public Optional<Product> findByProductName(String name)throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_PRODUCTNAME_SQL)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Product product = map(rs);
                    return Optional.of(product);

                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Product> findAll()throws SQLException{
        //we made an arraylist to hold our products
        List<Product> allProducts = new ArrayList<>();

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(FIND_ALL_SQL)){

            try(ResultSet rs = ps.executeQuery()){

                //here we used while not if unlike the past functions cuz we want to loop the whole product table
                //not jsut the one item so its gonna loop till we get to the last item
                while(rs.next()){

                    //translate using mapper
                    Product product = map(rs);
                    //add each product into our array list
                    allProducts.add(product);

                }

            }
        }
        return allProducts;
    }


    @Override
    public List<Product> findLowStock(int threshold)throws SQLException{

        List<Product> lowStockProducts = new ArrayList<>();
        try(Connection conn = DBConnection.getConnection();
            //our FIND LOW STOCK SQL querry does the hard work for us and looks for anyhting that is less than the
            //threshold
        PreparedStatement ps = conn.prepareStatement(FIND_LOW_STOCK_SQL)){

            //we have our threshold when binding cuz its the number that is gonna be like our alert
            //so when our quantity gets to the threshold number it gives us an alert
            ps.setInt(1,threshold);

            try(ResultSet rs = ps.executeQuery()){

                while(rs.next()){

                    Product product = map(rs);
                    // here it looks for all the
                    lowStockProducts.add(product);
                }
            }
        }
        return lowStockProducts;
    }

}

