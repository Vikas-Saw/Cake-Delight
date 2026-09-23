package com.cakedelight.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cakedelight.user.User;
import com.cakedelight.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {

        // Prevent duplicate email registration
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        // Set new accounts as ACTIVE
        user.setAccountStatus("ACTIVE");

        // Encrypt password before saving
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email);

        // Only ACTIVE users can log in
        if (user != null
                && "ACTIVE".equalsIgnoreCase(user.getAccountStatus())
                && passwordEncoder.matches(
                        password,
                        user.getPassword())) {

            return user;
        }

        return null;
    }
}