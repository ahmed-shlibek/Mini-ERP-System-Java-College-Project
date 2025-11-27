package main.java.dao_impl;

import main.java.dao.ProductDAO;
import main.java.database.DBConnection;
import main.java.model.Product;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductDAOImpl implements ProductDAO {

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (product_id , name ,category_id, price,quantity ) VALUES (?,?,?,?,?)";
    private static final String UPDATE_PRODUCT_SQL = "UPDATE products SET price =? , SET quantity =? WHERE product_id= ?";
    private static final String DELETE_PRODUCT_SQL = "DELETE FROM products where product_id = ?";
    private static final String FIND_BY_PRODUCTID_SQL ="SELECT * FROM products WHERE product_id =?";
    private static final String FIND_BY_PRODUCTNAME_SQL = "SELECT * FROM products WHERE name=?";
    private static final String FIND_ALL_SQL = "SELECT * FROM products";
    private static final String FIND_LOW_STOCK_SQL = "SELECET * FROM products WHERE quantity < ?";


    //uuid object to 16 byte
    private byte[] uuidToByte(UUID uuid){
        if (uuid == null) {
        return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

        //for each 8bytes allocate 64 so one has most sig and one has least sig to make 16 byte (128)
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();

    }

    private UUID byteToUUID(byte[] bytes){
        if(bytes == null || bytes.length <16){
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes);

        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        //reutrns our uuid after we it from byte form
        return new UUID(firstLong , secondLong);
    }

    private Product mapResultSetToProduct (ResultSet rs)throws SQLException{
        byte[] pidBytes = rs.getBytes("product_id");
        //basically got the product id from the data base and then changed it to our uuid
        UUID productId = byteToUUID(pidBytes);

        //also had to convert the category id
        byte[] cidBytes = rs.getBytes("category_id");
        UUID categoryId = byteToUUID(cidBytes);

        String name = rs.getString("name");
        double price = rs.getDouble("price");
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
    public void insert(Product product)throws SQLException{
        if(product.getProductId() == null){
            product.setProductId(UUID.randomUUID());
        }
        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(INSERT_PRODUCT_SQL)){

            byte[] productIdBytes = uuidToByte(product.getProductId());
            byte[] categoryIdBytes = uuidToByte(product.getCategoryId());

            ps.setBytes(1,productIdBytes);
            ps.setString(2,product.getName());
            ps.setBytes(3,categoryIdBytes);
            ps.setDouble(4,product.getPrice());
            ps.setInt(5,product.getQuantity());

            ps.executeUpdate();
        }catch(SQLException e){

            System.err.println("Error Inserting Product "+ product.getName());
            System.err.println("SQL State: "+ e.getSQLState() + "Error Code: "+ e.getErrorCode());
            e.printStackTrace();

            throw e;
        }
    }

    @Override
    public void update(Product product)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(UPDATE_PRODUCT_SQL)){

            ps.setDouble(1,product.getPrice());
            ps.setInt(2,product.getQuantity());
            ps.setBytes(3,uuidToByte(product.getProductId()));

            int rowsaffected = ps.executeUpdate();

            if(rowsaffected == 0){
                System.err.println("Warning rows affected :"+ rowsaffected + "the product id:"+product.getProductId());
            }
        }catch(SQLException e){

            System.err.println("Error Updating product "+ product.getName());
            System.err.println("SQL State:"+e.getSQLState()+"Error Code:" +e.getErrorCode());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void delete(UUID productId)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(DELETE_PRODUCT_SQL)){

            ps.setBytes(1,uuidToByte(productId));
            int affectedrow = ps.executeUpdate();

            if(affectedrow != 1){
                System.err.println("Warning affected row :"+ affectedrow + " The Product id:"+ productId);

            }
        }catch(SQLException e){
            System.err.println("Error Deleting product" + productId);
            System.err.println("SQLState:"+ e.getSQLState()+"Error Code:"+e.getErrorCode());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Optional<Product> findByProductId(UUID productId)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(FIND_BY_PRODUCTID_SQL)){

            ps.setBytes(1,uuidToByte(productId));

            try(ResultSet rs = ps.executeQuery()){

                if(rs.next()){
                    //the mapper function translates our data into objects (basically a language that java
                    //understands it
                    Product product = mapResultSetToProduct(rs);

                    //we use optional cuz it might return and might not so basically optional
                    return Optional.of(product);

                    // if user found we return user if not we return an empty container
                    //the query worked but did not find the product
                }return Optional.empty();

            }catch(SQLException e){

                System.err.println("Error Product not found" + e.getMessage());
            }

            //we have this return if the querry failed so we return empty
        }return Optional.empty();
    }


    @Override
    public Optional<Product> findByProductName(String name)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(FIND_BY_PRODUCTNAME_SQL)){
            ps.setString(1,name);

            try(ResultSet rs = ps.executeQuery()){

                if(rs.next()) {

                    Product product = mapResultSetToProduct(rs);
                    return Optional.of(product);

                    }return Optional.empty();
                }catch(SQLException e){

                //we use e.getMessage cuz it prints a human readable error
                System.err.println("Error did not find product"+ e.getMessage());
            }
            }return Optional.empty();
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
                    Product product = mapResultSetToProduct(rs);
                    //add each product into our array list
                    allProducts.add(product);

                }

            }
        }catch(SQLException e){

            System.err.println("Error not able to retrieve any Products");
            System.err.println("SQL State:"+ e.getSQLState() +"Error Code:"+e.getErrorCode());
            e.printStackTrace();

            throw e;
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

                    Product product = mapResultSetToProduct(rs);
                    // here it looks for all the
                    lowStockProducts.add(product);
                }
            }
        }catch(SQLException e){

            System.err.println("Error finding low stock products -- Threshold:"+ threshold);
            System.err.println("SQL State:"+ e.getSQLState()+ "Error Code:" +e.getErrorCode());

            throw e;
        }
        return lowStockProducts;
    }

}

