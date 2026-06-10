package com.bookreview.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value; 

@Component
public class JwtUtil {
    
    // 1. Constants
    @Value("${jwt.secret}")
    private String secret;  

    @Value("${jwt.expiration:3600000}")
    private long expirationTime;
    
    // 2. Helper method to get signing key (you can copy this exactly)
    private Key getSigningKey() { 
            byte[] keyBytes = secret.getBytes();
            return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // 3. Generate token 
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() +expirationTime );
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
     }
    
    // 4. Extract user ID 
    public String extractUserId(String token) {
         Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    
    // 5. Validate token 
    public boolean validateToken(String token) { 
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
     }
}