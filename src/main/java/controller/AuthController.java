package main.java.controller;

import main.java.model.User;
import main.java.service.UserService;
import main.java.util.SessionUtil;

public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public boolean login(String username, String password) {
        User user = userService.login(username, password);

        if (user != null) {
            SessionUtil.setCurrentUser(user);
            return true;
        }

        return false;
    }
}
