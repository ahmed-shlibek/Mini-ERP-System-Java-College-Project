package main.java.service;


import main.java.dao.UserDAO;
import main.java.model.User;
import main.java.util.SecurityUtil;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;


public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createUser(User user) {
        if(user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if(user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if(user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if(user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role  cannot be null or empty");
        }

        try {
            if(userDAO.findByUserName(user.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists.");
            }

            String hashedPassword = SecurityUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            return userDAO.save(user);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while creating user: " + user.getUsername(), e);
        }
    }

    public User login(String username, String password) {
        try {
            Optional<User> userOpt = userDAO.findByUserName(username);

            if(userOpt.isEmpty()) {
                throw new SecurityException("Invalid credentials.");
            }

            User user = userOpt.get();

            if(! SecurityUtil.verifyPassword(password, user.getPassword())) {
                throw new SecurityException("Invalid credentials.");
            }

            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Database error during login for user: " + username, e);
        }
    }

    public Optional<User> getUserByName(String username) {
        if(username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        try {
            return userDAO.findByUserName(username);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while getting user: " + username, e);
        }
    }

    public Optional<User> getUserById(UUID id) {
        if(id == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        try {
            return userDAO.findUserById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while getting user: " + id, e);
        }
    }

    public User updateUser(User user) {
        if(user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if(user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if(user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if(user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role  cannot be null or empty");
        }

        try {
            if(userDAO.findUserById(user.getUserId()).isEmpty()) {
                throw new IllegalArgumentException("Cannot update user. User with ID " + user.getUserId() + " not found.");
            }

            Optional<User> existingUserWithName = userDAO.findByUserName(user.getUsername());

            if (existingUserWithName.isPresent()) {
                if (!existingUserWithName.get().getUserId().equals(user.getUserId())) {
                    throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken by another user.");
                }
            }

            return userDAO.update(user);

        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while updating user: " + user.getUsername(), e);
        }
    }

    public void deleteUser(UUID id) {
        if(id == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        try {
            if(userDAO.findUserById(id).isEmpty()) {
                throw new IllegalArgumentException("Cannot delete user. User with ID " + id + " not found.");
            }

            userDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while deleting user: " + id, e);
        }
    }
}
