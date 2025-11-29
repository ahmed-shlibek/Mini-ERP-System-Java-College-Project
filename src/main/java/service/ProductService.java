package main.java.service;

//we dont need to import the DAOimpl cuz this helps us have our service layer be more dependant and does not
//need the daoimpl and only needs to have the dao , even if we chagned our data form we would only need to
//change the daoimpl not the service
import main.java.dao.ProductDAO;
import main.java.model.Product;
import main.java.model.Category;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductService {

    //this basically tells us to use productservices we need an object of the productdao,
    //were basically here using dependecy
    private final ProductDAO productDAO;
    public ProductService(ProductDAO productDAO){
        this.productDAO = productDAO;
    }

    //we throw the sqlexception from the dao to service layer so this layer can know that there is an exception happening
    public Product insertProduct (Product product) throws SQLException {
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        if(product.getName() == null || product.getName().isEmpty()){
            throw new IllegalArgumentException("Product cannot be null");
        }

        //this will basically make the first letter of product Uppercase and the rest Lower. it also checks if the
        //product name is null or not
        String normalizedString = product.getName();
        if(normalizedString != null && !normalizedString.isEmpty()){
           normalizedString = normalizedString.substring(0,1).toUpperCase()+ normalizedString.substring(1).toLowerCase();
        }
        if(productDAO.findByProductName(normalizedString).isPresent()){
            throw new IllegalArgumentException("Product with name "+product.getName()+"already exists");
        }

        //this made me change return type from void to Product
       return productDAO.insert(product);

    }

    Optional<Product> findByProductId(UUID productId)throws SQLException{
        if(productId == null){
            throw new IllegalArgumentException("Product Id cannot be null");
        }
        try{
            return productDAO.findByProductId(productId);
        }catch(SQLException e){

            System.err.println("DB error: failed to retrieve product ID"+ productId);
            e.printStackTrace();

            throw new RuntimeException("DB error occurred while getting product id",e);
        }
    }


    public Product update(Product product)throws SQLException{
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }
        //1- this is initial validation
        //checks if uuid is null
        if(product.getProductId()==null){
            throw new IllegalArgumentException("Product Id cannot be null");
        }
        //checks if name is null or if the name input is empty might be just blanks and we do not want that
        if(product.getName() == null || product.getName().isEmpty()){
            throw new IllegalArgumentException("Product Name cannot be null");
        }
        //checks if price or quantity is negative
        if(product.getPrice()<0  || product.getQuantity()<0){
            throw new IllegalArgumentException("Cannot be negative");
        }

        //2-We ensure our target we want to update Exists
        try {
            if (productDAO.findByProductId(product.getProductId()).isEmpty()) {
                throw new IllegalArgumentException("Did not find Product cannot update");
            }
            // now we check that no product has the same name
            Optional<Product> existingProductName = productDAO.findByProductName(product.getName());

            //this will return empty if name is not present and we would just skip rest of code and update
            //if its present we get its id
            if(existingProductName.isPresent()) {
                UUID existingId = existingProductName.get().getProductId();

                //if the id is present then that means its a dupilacte
                if (!existingId.equals(product.getProductId())){
                    throw new IllegalArgumentException("Product name '" + product.getName() + "' is already in use by another product.");
                }
            }

            //now we collab with category
            //this checks if product actually has a category id
            if(product.getCategoryId() != null){

                //this gets our category id from product
                Optional<Category> referencedCategory = CategoryService.getCategoryById(product.getCategoryId());

                if(referencedCategory.isEmpty()){
                    throw new IllegalArgumentException("Invalid Category ID the specified Catefory "+ product.getCategoryId() + "does not exist");
                }
            }


            return productDAO.update(product);
        }catch(SQLException e){
            System.err.println("DB Error updating Product" + e.getMessage());
            throw new RuntimeException("A DB error prevented the product Update",e);
        }

    }

    List<Product> findLowStock(int threshold)throws SQLException{

        if(threshold < 0){
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        try{
            //after we cheked if the threshold is not negative we can now find the low stock
            return productDAO.findLowStock(threshold);
        }catch(SQLException e){
            System.err.println("DATABASE ERROR: Failed to retrieve low stock products for threshold " + threshold);
            e.printStackTrace();

            throw new RuntimeException("A data access error occurred while fetching low stock products.", e);
        }
    }

}
