package main.java.dao_impl;

import main.java.dao.UserDAO;
import main.java.database.DBConnection;
import main.java.model.User;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDAOImpl implements UserDAO {

    private static final String INSERT_USER_SQL = "INSERT INTO users (user_id, username, password, role) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_USERNAME_SQL = "SELECT user_id, username, password, role FROM users WHERE username = ?";
    private static final String FIND_BY_ID_SQL = "SELECT user_id, username, password, role FROM users WHERE user_id = ?";
    private static final String FIND_ALL_SQL = "SELECT user_id, username, password, role FROM users";
    private static final String UPDATE_USER_SQL = "UPDATE users SET username = ?, password =? , role = ? WHERE user_id = ?";
    private static final String DELETE_BY_USER_ID_SQL ="DELETE FROM users WHERE user_id =?";

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
    public User save(User user) throws SQLException{
        if(user.getUserId()== null){
            user.setUserID(UUID.randomUUID());
        }
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL)) {

        byte[] userIdBytes = uuidToBytes(user.getUserId());

        //the numbers are placeholders for the ? in sql query
        //ps.setString sends the value with a placeholder to our database
        ps.setBytes(1,userIdBytes);
        ps.setString(2,user.getUsername());
        ps.setString(3,user.getPassword());
        ps.setString(4,user.getRole());

        //basically tells it to update our sql
        ps.executeUpdate();
        }

        return user;
    }


    @Override
    public Optional<User> findByUserName(String username) throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {
            ps.setString(1, username);

            //ps.executequerry is a read operation
            try (ResultSet rs = ps.executeQuery()) {
                //made result set querry then we see if anything returned if yes make the data row into a java object
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    //return it
                    return Optional.of(user);
                }
                //returned empty if did not work
            }
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findUserById(UUID id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            stmt.setBytes(1, uuidToBytes(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL)) {
            List<User> users = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    users.add(user);
                }
            }

            return users;
        }
    }

    @Override
    public User update(User user) throws SQLException{
        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(UPDATE_USER_SQL)){
            //we got the service layer to update these fields
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3,user.getRole());
            ps.setBytes(4,uuidToBytes(user.getUserId()));

            //when we executed the update we got the whole ps to go the the sql to be executed
            //we called it and then it reutrns a number if 1 that means all goof if anything else then error
            ps.executeUpdate();
        }

        return user;
    }

    @Override
    public void delete(UUID userId)throws SQLException{

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(DELETE_BY_USER_ID_SQL)){

            ps.setBytes(1,uuidToBytes(userId));

            ps.executeUpdate();
        }
    }
}


