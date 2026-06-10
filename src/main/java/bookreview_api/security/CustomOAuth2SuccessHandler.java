package com.bookreview.api.security;

import com.bookreview.api.model.User;
import com.bookreview.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

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
        
        // Build HTML response using string concatenation (Java 8 compatible)
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>Login Successful</title>\n");
        html.append("    <style>\n");
        html.append("        body {\n");
        html.append("            font-family: Arial, sans-serif;\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: center;\n");
        html.append("            align-items: center;\n");
        html.append("            height: 100vh;\n");
        html.append("            margin: 0;\n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            text-align: center;\n");
        html.append("            padding: 2rem;\n");
        html.append("        }\n");
        html.append("        .spinner {\n");
        html.append("            width: 50px;\n");
        html.append("            height: 50px;\n");
        html.append("            border: 3px solid rgba(255,255,255,0.3);\n");
        html.append("            border-radius: 50%;\n");
        html.append("            border-top-color: white;\n");
        html.append("            animation: spin 1s ease-in-out infinite;\n");
        html.append("            margin: 0 auto 20px;\n");
        html.append("        }\n");
        html.append("        @keyframes spin {\n");
        html.append("            to { transform: rotate(360deg); }\n");
        html.append("        }\n");
        html.append("        h2 { margin-bottom: 10px; }\n");
        html.append("        p { opacity: 0.8; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"spinner\"></div>\n");
        html.append("        <h2>Login Successful!</h2>\n");
        html.append("        <p>Redirecting you back to the app...</p>\n");
        html.append("    </div>\n");
        html.append("    <script>\n");
        html.append("        const data = {\n");
        html.append("            type: 'oauth-success',\n");
        html.append("            token: '").append(jwtToken).append("',\n");
        html.append("            userId: '").append(user.getId()).append("',\n");
        html.append("            name: '").append(escapeJavaScript(user.getName())).append("',\n");
        html.append("            email: '").append(user.getEmail()).append("',\n");
        html.append("            role: '").append(user.getRole()).append("'\n");
        html.append("        };\n");
        html.append("        \n");
        html.append("        if (window.opener) {\n");
        html.append("            window.opener.postMessage(data, 'http://localhost:3000');\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        setTimeout(function() {\n");
        html.append("            window.close();\n");
        html.append("        }, 1500);\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html.toString());
    }
    
    private String escapeJavaScript(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}