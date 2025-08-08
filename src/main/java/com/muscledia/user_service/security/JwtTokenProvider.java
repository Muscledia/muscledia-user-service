package com.muscledia.user_service.security;

import com.muscledia.user_service.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long validityInMilliseconds;

    private final UserDetailsService userDetailsService;
    private SecretKey key;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    // Enhanced token creation with roles
    public String createToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId().toString());
        claims.put("userIdLong", user.getUserId());
        claims.put("sub", user.getUsername());
        claims.put("uuidString", user.getUuidString());

        // CRITICAL FIX: Include roles without "ROLE_" prefix in JWT
        // The authorities will be prefixed with "ROLE_" during authentication
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name().replace("ROLE_", "")) // Remove ROLE_ prefix if present
                .collect(Collectors.toList());
        claims.put("roles", roles);

        System.out.println("Creating token with roles: " + roles); // Debug log

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    // Legacy method for backward compatibility
    public String createToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("userIdLong", userId);
        claims.put("sub", username);

        // For legacy method, load user to get roles
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            claims.put("roles", roles);
        } catch (Exception e) {
            // If can't load user, default to USER role
            claims.put("roles", List.of("USER"));
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();

        // Extract roles from JWT token and add ROLE_ prefix
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        } else {
            // Default to USER role if no roles found
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        System.out.println("Token roles: " + roles + " -> Authorities: " + authorities); // Debug log

        // Create a custom UserDetails object with the extracted information
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("") // Password not needed for JWT authentication
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getUserIdAsString(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", String.class);
    }

    public Long getUserId(String token) {
        String userIdStr = getUserIdAsString(token);
        return userIdStr != null ? Long.valueOf(userIdStr) : null;
    }

    public List<String> getRoles(String token) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles");
        return roles != null ? roles : new ArrayList<>();
    }

    public Map<String, Object> getUserClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}