package main.java.dao;
import main.java.model.User;


import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;


public interface UserDAO {

    //this is a method that returns a user by their unique name
    // if not in table return Optional.empty();
    Optional<User> findByUsername(String username) throws SQLException;

    //this will insert a new user in database
    void insert(User user) throws SQLException;

    //this will update the user in the database
    void update(User user) throws SQLException;

    //this will delete user by user id
    void delete(UUID userId)throws SQLException;


}
