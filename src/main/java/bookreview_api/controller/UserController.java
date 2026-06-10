package com.bookreview.api.controller;

import com.bookreview.api.model.User;
import com.bookreview.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private User getCurrentUser() {
        return userRepository.findById(getCurrentUserId()).orElse(null);
    }

    // Get all users - ADMIN only
    @GetMapping
    public List<User> getAllUsers() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied. Admin only.");
        }
        return userRepository.findAll();
    }

    // Get current user's profile
    @GetMapping("/me")
    public User getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        return currentUser;
    }

    // Update current user's profile
    @PutMapping("/me")
    public Map<String, String> updateCurrentUser(@RequestBody Map<String, String> updates) {
        String currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElse(null);
        Map<String, String> response = new HashMap<>();
        
        if (user == null) {
            response.put("error", "User not found");
            return response;
        }
        
        if (updates.containsKey("name") && updates.get("name") != null) {
            user.setName(updates.get("name"));
        }
        
        userRepository.save(user);
        
        response.put("message", "Profile updated successfully");
        response.put("name", user.getName());
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        
        return response;
    }

    // Upload profile picture
    @PostMapping("/profile/picture")
    public Map<String, String> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        String currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElse(null);
        Map<String, String> response = new HashMap<>();
        
        if (user == null) {
            response.put("error", "User not found");
            return response;
        }
        
        try {
            // Create upload directory
            String uploadDir = "uploads/profiles/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            
            // Generate unique filename
            String filename = currentUserId + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = Paths.get(uploadDir + filename);
            Files.write(filePath, file.getBytes());
            
            // Save URL to user
            String fileUrl = "/uploads/profiles/" + filename;
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);
            
            response.put("message", "Profile picture uploaded successfully");
            response.put("profilePictureUrl", fileUrl);
        } catch (IOException e) {
            response.put("error", "Failed to upload file: " + e.getMessage());
        }
        
        return response;
    }

    // Get user by ID - ADMIN can see any, USER can see only themselves
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable String userId) {
        User currentUser = getCurrentUser();
        String currentUserId = getCurrentUserId();
        
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            return userRepository.findById(userId).orElse(null);
        }
        
        if (!userId.equals(currentUserId)) {
            throw new RuntimeException("Access denied. You can only view your own profile.");
        }
        
        return userRepository.findById(userId).orElse(null);
    }

    // Delete user - ADMIN only
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable String userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied. Admin only.");
        }
        
        userRepository.deleteById(userId);
        return "User deleted successfully: " + userId;
    }

    // Update user role - ADMIN only
    @PutMapping("/{userId}/role")
    public String updateUserRole(@PathVariable String userId, @RequestParam String role) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied. Admin only.");
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found: " + userId;
        }
        
        user.setRole(role);
        userRepository.save(user);
        return "User role updated to: " + role;
    }
}