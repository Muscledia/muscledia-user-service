package com.muscledia.user_service.user.dto;

import com.muscledia.user_service.user.entity.ActivityLevel;
import com.muscledia.user_service.user.entity.GoalType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for calculating daily calorie needs.
 * Contains all necessary parameters for BMR and TDEE calculations.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalorieCalculationRequest {

    @NotNull(message = "Current weight is required")
    @Positive(message = "Weight must be positive")
    @DecimalMax(value = "500.0", message = "Weight must be less than 500 kg")
    private Double currentWeight; // in kg

    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
    private Double height; // in cm

    @NotNull(message = "Age is required")
    @Min(value = 15, message = "Age must be at least 15")
    @Max(value = 120, message = "Age must be less than 120")
    private Integer age;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|male|female)$", message = "Gender must be MALE or FEMALE")
    private String gender;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    @Positive(message = "Target weight must be positive if provided")
    @DecimalMax(value = "500.0", message = "Target weight must be less than 500 kg")
    private Double targetWeight; // Optional, for context


}
