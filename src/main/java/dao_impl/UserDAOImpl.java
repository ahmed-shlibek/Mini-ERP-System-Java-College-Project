package main.java.dao_impl;

import main.java.dao.UserDAO;
import main.java.database.DBConnection;
import main.java.model.User;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class UserDAOImpl implements UserDAO {

    private static final String INSERT_USER_SQL = "INSERT INTO users (user_id, username, password, role) VALUES (?, ?, ?, ?);";
    private static final String FIND_BY_USERNAME_SQL = "SELECT user_id, username, password, role FROM Users WHERE username = ? ";
    private static final String UPDATE_USER_SQL = "UPDATE users SET password =? , role ? WHERE user_id = ?";

    //this will convert a uuid object(128 bits) to 16byte for efficency
    //return type : byte[]
    //takes in a uuid type
    private byte[] uuidToBytes(UUID uuid){
        if(uuid == null){
            return null;
        }
        //allocates 16 bytes for the uuid
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

        //this basicaly gets us two 64 bits of the 128 -- 64 for msb and 64 lsb
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        //we dont have to write return bb.byte[] cuz we already declared it
        return bb.array();
    }

    //from the 16 byte to original 128
    //return type: UUID
    // takes in byte[] type
    private UUID bytesToUUID(byte[] bytes){
        if (bytes == null || bytes.length < 16){
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        long firstLong = bb.getLong();
        long secondLong = bb.getLong();

        return new UUID(firstLong , secondLong);
    }

    //we have this method to basically translate from oop to relation sql
    private User mapResultSetToUser(ResultSet rs)throws SQLException {
        byte[] idBytes = rs.getBytes("user_id");
        UUID userId = bytesToUUID(idBytes);
        //retrieve the rest
         String username = rs.getString("username");
         String role = rs.getString("role");
         String password = rs.getString("password");

         //we are populating so basically filling in
         User user = new User();
         user.setUserID(userId);
         user.setRole(role);
         user.setPassword(password);
         user.setUsername(username);

         return user;
    }

    @Override
    public void insert(User user)throws SQLException{
        if(user.getUserID()== null){
            user.setUserID(UUID.randomUUID());
        }
        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL)){

        byte[] userIdBytes = uuidToBytes(user.getUserID());
        //the numbers are place holders for the ? in sql query
            //ps.setstring sends the value with a place holder to our database
        ps.setBytes(1,userIdBytes);
        ps.setString(2,user.getUsername());
        ps.setString(3,user.getPassword());
        ps.setString(4,user.getRole());

        //basically tells it to update our sql
        ps.executeUpdate();

        }catch(SQLException e){

            //looks for username only cuz its easier to find for the dev
            System.err.println("Error inserting User :" + user.getUsername());
            //this describes the nature of the error
            System.err.println("SQL State: "+ e.getSQLState() + "Error Code:"+ e.getErrorCode());
            e.printStackTrace();//prints the stack trace for easy debugging

            //throws exception to service layer
            throw e;
        }
    }


    @Override
    public Optional<User> findByUsername(String username) throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {
            ps.setString(1, username);

            //ps.executequerry is a read operation
            try(ResultSet rs = ps.executeQuery()){
                //made result set querry then we see if anything returned if yes make the data row into a java object
                if(rs.next()){
                    User user = mapResultSetToUser(rs);
                    //return it
                    return Optional.of(user);
                }
                //returned empty if did not work
            }return Optional.empty();

        } catch (SQLException e) {
            System.err.println("Data Base error while looking for username " + e.getMessage());
        }
        // we return this because so we can say the database querry ran succesfully but no matching username was found
        return Optional.empty();
    }


    @Override
    public void update(User user) throws SQLException{
        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(UPDATE_USER_SQL)){
            //we got the service layer to update these fields
            ps.setString(1, user.getPassword());
            ps.setString(2,user.getRole());
            ps.setBytes(3,uuidToBytes(user.getUserID()));

            //when we executed the update we got the whole ps to go the the sql to be executed
            //we called it and then it reutrns a number if 1 that means all goof if anything else then error
            int rowsAffected = ps.executeUpdate();
            //if we get 1 that means the update was succseful
            if(rowsAffected !=1){
                System.err.println("Warning update operation affected : "+rowsAffected + "the row for userid :"+ user.getUserID());
            }

        }catch(SQLException e) {

            System.err.println("Error updating user ID"+ user.getUserID());
            System.err.println("SQL State "+ e.getSQLState() +"Error code:"+ e.getErrorCode());
            e.printStackTrace();
            throw e;
        }
        }

    }


