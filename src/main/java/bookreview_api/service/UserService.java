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

    // Register new user - Let JPA generate UUID automatically
    public User register(String name, String email, String rawPassword, String role) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return null;
        }

        // Create user WITHOUT setting ID - JPA will generate UUID automatically
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRole(role != null ? role : "USER");
        newUser.setCreatedAt(LocalDateTime.now());
        
        // Don't set ID here! Let JPA/Hibernate generate it
        
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
    
    // Find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // Check if email exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Update user profile
    public User updateUser(String userId, String name, String profilePictureUrl) {
        User user = findById(userId);
        if (user != null) {
            if (name != null && !name.isEmpty()) {
                user.setName(name);
            }
            if (profilePictureUrl != null) {
                user.setProfilePictureUrl(profilePictureUrl);
            }
            return userRepository.save(user);
        }
        return null;
    }
}
