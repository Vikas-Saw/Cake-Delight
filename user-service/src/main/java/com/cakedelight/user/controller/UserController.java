package com.cakedelight.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.user.User;
import com.cakedelight.user.service.UserService;
import com.cakedelight.user.security.AuthResponse;
import com.cakedelight.user.security.JwtService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {

        User savedUser = userService.registerUser(user);

        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(
            @PathVariable String email) {

        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody User user) {

        User loggedInUser =
                userService.loginUser(
                        user.getEmail(),
                        user.getPassword()
                );

        if (loggedInUser == null) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.generateToken(
                loggedInUser.getId(),
                loggedInUser.getEmail(),
                loggedInUser.getName()
        );

        AuthResponse response = new AuthResponse(
                token,
                loggedInUser.getId(),
                loggedInUser.getName(),
                loggedInUser.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}