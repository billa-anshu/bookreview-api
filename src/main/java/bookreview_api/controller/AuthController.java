package com.bookreview.api.controller;

import com.bookreview.api.model.User;
import com.bookreview.api.security.JwtUtil;
import com.bookreview.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Register endpoint
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");
        String role = request.getOrDefault("role", "USER");

        User newUser = userService.register(name, email, password, role);

        Map<String, String> response = new HashMap<>();

        if (newUser == null) {
            response.put("error", "Email already exists");
            return response;
        }

        String token = jwtUtil.generateToken(newUser.getId());
        response.put("token", token);
        response.put("userId", newUser.getId());
        response.put("name", newUser.getName());
        response.put("email", newUser.getEmail());
        response.put("role", newUser.getRole());
        response.put("message", "Registration successful");
        return response;
    }

    // Login endpoint
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userService.authenticate(email, password);

        Map<String, String> response = new HashMap<>();

        if (user == null) {
            response.put("error", "Invalid email or password");
            return response;
        }

        String token = jwtUtil.generateToken(user.getId());
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("message", "Login successful");
        return response;
    }

    // Logout endpoint
    @PostMapping("/logout")
    public Map<String, String> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return response;
    }
}