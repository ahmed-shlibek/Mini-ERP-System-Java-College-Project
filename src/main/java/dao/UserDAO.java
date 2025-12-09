package main.java.dao;
import main.java.model.User;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserDAO {


    // this will save a new user in database
    User save(User user) throws SQLException;

    // this is a method that returns a user by their unique name
    // if not in table return Optional.empty();
    Optional<User> findByUserName(String username) throws SQLException;

    Optional<User> findUserById(UUID id) throws SQLException;

    List<User> findAll() throws SQLException;

    // this will update the user in the database
    User update(User user) throws SQLException;

    // this will delete user by user id
    void delete(UUID userId) throws SQLException;


}
