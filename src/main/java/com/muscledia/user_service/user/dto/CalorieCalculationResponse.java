package com.muscledia.user_service.user.dto;


import lombok.*;

/**
 * Response DTO containing calculated daily calorie recommendations.
 * Includes breakdown of BMR, TDEE, and goal-adjusted calories.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalorieCalculationResponse {

    /**
     * Basal Metabolic Rate - calories burned at rest
     */
    private Double bmr;

    /**
     * Total Daily Energy Expenditure - calories burned with activity
     */
    private Double tdee;

    /**
     * Recommended daily calorie intake adjusted for goals
     */
    private Double recommendedCalories;

    /**
     * Calorie adjustment applied based on goal (+/- from TDEE)
     */
    private Double calorieAdjustment;

    /**
     * The goal type used for this calculation
     */
    private String goalType;

    /**
     * The activity level used for this calculation
     */
    private String activityLevel;

    /**
     * Whether the result was adjusted due to minimum calorie floor
     */
    private Boolean adjustedForMinimum;

    /**
     * Whether the result was adjusted due to maximum deficit cap
     */
    private Boolean adjustedForMaximumDeficit;

    /**
     * Health disclaimer message
     */
    private String disclaimer;

    /**
     * Additional notes or warnings
     */
    private String notes;
}
