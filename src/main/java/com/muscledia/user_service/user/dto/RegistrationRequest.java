package com.muscledia.user_service.user.dto;

import com.muscledia.user_service.avatar.entity.AvatarType;
import com.muscledia.user_service.user.entity.GoalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    @NotBlank(message = "Username is required")
    @Size(max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private LocalDate birthDate;

    @Size(max = 10)
    private String gender;

    private Double height;

    private Double initialWeight;

    private GoalType goalType;

    private AvatarType initialAvatarType;
}
