package com.bookreview.api.service;

import com.bookreview.api.model.User;
import com.bookreview.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Register new user
    public User register(String name, String email, String rawPassword, String role) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return null;
        }

        // Generate user ID
        long count = userRepository.count();
        String userId = "U" + (count + 1000);
        
        // Hash password
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Create and save user
        User newUser = new User(userId, name, email, encodedPassword, role);
        newUser.setCreatedAt(LocalDateTime.now());  // Set current timestamp
        return userRepository.save(newUser);
    }

    // Authenticate user (login)
    public User authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    // Find user by ID
    public User findById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
}