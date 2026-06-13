package com.bookreview.api.security;

import com.bookreview.api.model.User;
import com.bookreview.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }
        
        if (email == null || email.isEmpty()) {
            String login = oAuth2User.getAttribute("login");
            email = login + "@github.com";
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            long userCount = userRepository.count();
            String userId = "U" + (userCount + 1000);
            user = new User();
            user.setId(userId);
            user.setName(name);
            user.setEmail(email);
            user.setPassword("");
            user.setRole("USER");
            userRepository.save(user);
        }
        
        String jwtToken = jwtUtil.generateToken(user.getId());
        
        // Redirect to frontend with token in URL (most reliable method)
        String redirectUrl = String.format(
            "%s/login?token=%s&userId=%s&name=%s&email=%s&role=%s",
            frontendUrl,
            jwtToken,
            user.getId(),
            URLEncoder.encode(user.getName(), "UTF-8"),
            user.getEmail(),
            user.getRole()
        );
        
        System.out.println("OAuth Success - Redirecting to: " + redirectUrl);
        
        // Send redirect to frontend
        response.sendRedirect(redirectUrl);
    }
}
