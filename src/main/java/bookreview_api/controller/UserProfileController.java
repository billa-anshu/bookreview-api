package com.bookreview.api.controller;

import com.bookreview.api.model.User;
import com.bookreview.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Upload profile picture
    @PostMapping("/picture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            String currentUserId = getCurrentUserId();
            User user = userRepository.findById(currentUserId).orElse(null);
            
            if (user == null) {
                return "User not found";
            }
            
            // Create upload directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + File.separator + filename);
            Files.write(path, file.getBytes());
            
            // Save file path to user
            String fileUrl = "/uploads/" + filename;
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);
            
            return "Profile picture uploaded: " + fileUrl;
        } catch (IOException e) {
            return "Upload failed: " + e.getMessage();
        }
    }

    // Get profile picture URL
    @GetMapping("/picture")
    public String getProfilePictureUrl() {
        String currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElse(null);
        
        if (user == null || user.getProfilePictureUrl() == null) {
            return "No profile picture set";
        }
        return user.getProfilePictureUrl();
    }

    // Update profile
    @PutMapping
    public String updateProfile(@RequestBody User profileUpdate) {
        String currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElse(null);
        
        if (user == null) {
            return "User not found";
        }
        
        if (profileUpdate.getName() != null) {
            user.setName(profileUpdate.getName());
        }
        
        userRepository.save(user);
        return "Profile updated successfully";
    }
}