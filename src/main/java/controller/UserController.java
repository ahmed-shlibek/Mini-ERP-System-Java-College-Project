package main.java.controller;

import main.java.model.User;
import main.java.service.UserService;

import java.util.List;
import java.util.UUID;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    public User createUser(String username, String password, String role) {
        User newUser = new User(username, password, role);
        return userService.createUser(newUser);
    }

    public User updateUser(User user) {
        return userService.updateUser(user);
    }


    public void deleteUser(UUID userId) {
        userService.deleteUser(userId);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}