package com.muscledia.user_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a new user registers in the system
 * This event triggers automatic gamification profile creation
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Registration date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant registrationDate;

    private Map<String, Object> userPreferences;
    private String goalType;
    private String initialAvatarType;
    private String eventType = "USER_REGISTERED";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    /**
     * Create event with current timestamp
     */
    public static UserRegisteredEvent create(Long userId, String username, String email,
                                             Instant registrationDate, String goalType,
                                             String initialAvatarType, Map<String, Object> userPreferences) {
        return UserRegisteredEvent.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .registrationDate(registrationDate)
                .goalType(goalType)
                .initialAvatarType(initialAvatarType)
                .userPreferences(userPreferences)
                .eventType("USER_REGISTERED")
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Validate event content
     */
    public boolean isValid() {
        return userId != null &&
                username != null && !username.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                registrationDate != null;
    }
}
