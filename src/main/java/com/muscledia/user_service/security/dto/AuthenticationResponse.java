package com.muscledia.user_service.security.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String username;
    // CRITICAL FIX: Serialize userId as String to avoid JavaScript precision issues
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    // Optional: Include UUID for additional reference
    private String uuidString;

    private List<String> roles; // Include user roles


    // Constructor without UUID (for backward compatibility)
    public AuthenticationResponse(String token, String username, Long userId) {
        this.token = token;
        this.username = username;
        this.userId = userId;
    }

    // Constructor with UUID but no roles
    public AuthenticationResponse(String token, String username, Long userId, String uuidString) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.uuidString = uuidString;
    }

}