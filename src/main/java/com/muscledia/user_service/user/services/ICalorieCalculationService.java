package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.dto.CalorieCalculationRequest;
import com.muscledia.user_service.user.dto.CalorieCalculationResponse;
import org.springframework.stereotype.Service;

/**
 * Service interface for calculating daily calorie needs based on user profile and goals.
 */
@Service
public interface ICalorieCalculationService {
    /**
            * Calculate daily calorie recommendations based on user data and goals.
            *
            * @param request The calorie calculation request containing user data
     * @return CalorieCalculationResponse with BMR, TDEE, and recommended calories
     */
    CalorieCalculationResponse calculateCalories(CalorieCalculationRequest request);

    /**
     * Calculate daily calorie recommendations using user ID.
     * Fetches user data from database.
     *
     * @param userId The user's ID
     * @return CalorieCalculationResponse with BMR, TDEE, and recommended calories
     * @throws IllegalStateException if user profile is incomplete
     */
    CalorieCalculationResponse calculateCaloriesForUser(Long userId);
}
