package com.muscledia.user_service.user.services;

import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.dto.CalorieCalculationRequest;
import com.muscledia.user_service.user.dto.CalorieCalculationResponse;
import com.muscledia.user_service.user.entity.ActivityLevel;
import com.muscledia.user_service.user.entity.GoalType;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Implementation of calorie calculation service using Mifflin-St Jeor equation.
 * Provides accurate daily calorie recommendations with safety constraints.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalorieCalculationServiceImpl implements ICalorieCalculationService {
    private final UserRepository userRepository;

    // Safety constraints
    private static final int MIN_CALORIES_FEMALE = 1200;
    private static final int MIN_CALORIES_MALE = 1500;
    private static final int MAX_DEFICIT = 1000;

    // Goal adjustments
    private static final int WEIGHT_LOSS_DEFICIT = 500;
    private static final int MUSCLE_GAIN_SURPLUS_MIN = 250;
    private static final int MUSCLE_GAIN_SURPLUS_MAX = 500;

    private static final String HEALTH_DISCLAIMER =
            "This calculation is for informational purposes only. Please consult with a healthcare " +
                    "professional or registered dietitian before making significant changes to your diet or exercise routine.";


    @Override
    public CalorieCalculationResponse calculateCalories(CalorieCalculationRequest request) {
        log.info("Calculating calories for user with weight: {}kg, height: {}cm, age: {}, gender: {}, " +
                        "activity: {}, goal: {}",
                request.getCurrentWeight(), request.getHeight(), request.getAge(),
                request.getGender(), request.getActivityLevel(), request.getGoalType());

        // Step 1: Calculate BMR using Mifflin-St Jeor equation
        double bmr = calculateBMR(
                request.getCurrentWeight(),
                request.getHeight(),
                request.getAge(),
                request.getGender()
        );

        // Step 2: Calculate TDEE (BMR * activity multiplier)
        double tdee = calculateTDEE(bmr, request.getActivityLevel());

        // Step 3: Adjust for goals
        GoalAdjustment adjustment = adjustForGoal(tdee, request.getGoalType());

        // Step 4: Apply safety constraints
        SafetyAdjustment safetyResult = applySafetyConstraints(
                adjustment.adjustedCalories,
                request.getGender(),
                tdee
        );

        // Build response
        return CalorieCalculationResponse.builder()
                .bmr(Math.round(bmr * 10.0) / 10.0) // Round to 1 decimal
                .tdee(Math.round(tdee * 10.0) / 10.0)
                .recommendedCalories(Math.round(safetyResult.finalCalories * 10.0) / 10.0)
                .calorieAdjustment(adjustment.adjustment)
                .goalType(request.getGoalType().name())
                .activityLevel(request.getActivityLevel().name())
                .adjustedForMinimum(safetyResult.wasAdjustedForMinimum)
                .adjustedForMaximumDeficit(safetyResult.wasAdjustedForMaxDeficit)
                .disclaimer(HEALTH_DISCLAIMER)
                .notes(buildNotes(request, safetyResult))
                .build();
    }

    @Override
    public CalorieCalculationResponse calculateCaloriesForUser(Long userId) {
        log.info("Calculating calories for user ID: {}", userId);

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validate user profile completeness
        validateUserProfile(user);

        // Calculate age from birthdate
        int age = calculateAge(user.getBirthDate());

        // Build request from user data
        // Note: ActivityLevel would need to be added to User entity or fetched from profile
        // For now, defaulting to MODERATELY_ACTIVE
        CalorieCalculationRequest request = CalorieCalculationRequest.builder()
                .currentWeight(user.getInitialWeight())
                .height(user.getHeight())
                .age(age)
                .gender(user.getGender())
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE) // Default or fetch from extended profile
                .goalType(user.getGoalType())
                .build();

        return calculateCalories(request);
    }

    /**
     * Calculate Basal Metabolic Rate using Mifflin-St Jeor equation.
     * Men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) + 5
     * Women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) - 161
     */
    private double calculateBMR(double weight, double height, int age, String gender) {
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);

        if ("MALE".equalsIgnoreCase(gender)) {
            bmr += 5;
        } else {
            bmr -= 161;
        }

        log.debug("Calculated BMR: {} for gender: {}", bmr, gender);
        return bmr;
    }

    /**
     * Calculate Total Daily Energy Expenditure.
     * TDEE = BMR × activity multiplier
     */
    private double calculateTDEE(double bmr, ActivityLevel activityLevel) {
        double tdee = bmr * activityLevel.getMultiplier();
        log.debug("Calculated TDEE: {} (BMR: {} × multiplier: {})",
                tdee, bmr, activityLevel.getMultiplier());
        return tdee;
    }

    /**
     * Adjust calories based on goal type.
     */
    private GoalAdjustment adjustForGoal(double tdee, GoalType goalType) {
        double adjustment = 0;
        double adjustedCalories = tdee;

        switch (goalType) {
            case LOSE_WEIGHT:
                adjustment = -WEIGHT_LOSS_DEFICIT;
                adjustedCalories = tdee - WEIGHT_LOSS_DEFICIT;
                log.debug("Weight loss goal: applying deficit of {} calories", WEIGHT_LOSS_DEFICIT);
                break;

            case GAIN_MUSCLE:
                // Use average of min and max surplus
                adjustment = (MUSCLE_GAIN_SURPLUS_MIN + MUSCLE_GAIN_SURPLUS_MAX) / 2.0;
                adjustedCalories = tdee + adjustment;
                log.debug("Muscle gain goal: applying surplus of {} calories", adjustment);
                break;

            case BUILD_STRENGTH:
                // Build strength typically means maintenance or slight surplus
                adjustment = 0;
                adjustedCalories = tdee;
                log.debug("Strength building goal: maintaining TDEE");
                break;

            default:
                log.warn("Unknown goal type: {}, defaulting to maintenance", goalType);
                break;
        }

        return new GoalAdjustment(adjustedCalories, adjustment);
    }

    /**
     * Apply safety constraints: minimum calorie floor and maximum deficit cap.
     */
    private SafetyAdjustment applySafetyConstraints(double calories, String gender, double tdee) {
        double finalCalories = calories;
        boolean adjustedForMin = false;
        boolean adjustedForMaxDeficit = false;

        // Determine minimum based on gender
        int minCalories = "MALE".equalsIgnoreCase(gender) ?
                MIN_CALORIES_MALE : MIN_CALORIES_FEMALE;

        // Check minimum calorie floor
        if (finalCalories < minCalories) {
            log.warn("Calculated calories {} below minimum {}. Adjusting to minimum.",
                    finalCalories, minCalories);
            finalCalories = minCalories;
            adjustedForMin = true;
        }

        // Check maximum deficit (only for weight loss)
        double deficit = tdee - finalCalories;
        if (deficit > MAX_DEFICIT) {
            log.warn("Deficit {} exceeds maximum {}. Capping deficit.",
                    deficit, MAX_DEFICIT);
            finalCalories = tdee - MAX_DEFICIT;
            adjustedForMaxDeficit = true;

            // Double-check we didn't go below minimum
            if (finalCalories < minCalories) {
                finalCalories = minCalories;
                adjustedForMin = true;
            }
        }

        return new SafetyAdjustment(finalCalories, adjustedForMin, adjustedForMaxDeficit);
    }

    /**
     * Build informative notes based on adjustments made.
     */
    private String buildNotes(CalorieCalculationRequest request, SafetyAdjustment safety) {
        StringBuilder notes = new StringBuilder();

        if (safety.wasAdjustedForMinimum) {
            String gender = "MALE".equalsIgnoreCase(request.getGender()) ? "men" : "women";
            int minCals = "MALE".equalsIgnoreCase(request.getGender()) ?
                    MIN_CALORIES_MALE : MIN_CALORIES_FEMALE;
            notes.append(String.format(
                    "Calories adjusted to minimum safe level (%d kcal/day for %s). ",
                    minCals, gender
            ));
        }

        if (safety.wasAdjustedForMaxDeficit) {
            notes.append(String.format(
                    "Deficit capped at maximum safe level (%d kcal/day). ",
                    MAX_DEFICIT
            ));
        }

        if (request.getGoalType() == GoalType.GAIN_MUSCLE) {
            notes.append(String.format(
                    "For muscle gain, aim for %d-%d calories above TDEE. ",
                    MUSCLE_GAIN_SURPLUS_MIN, MUSCLE_GAIN_SURPLUS_MAX
            ));
        }

        if (notes.length() == 0) {
            notes.append("Calculation within normal parameters. ");
        }

        notes.append("Remember to adjust based on your progress and how you feel.");

        return notes.toString();
    }

    /**
     * Validate that user has all required profile data.
     */
    private void validateUserProfile(User user) {
        if (user.getInitialWeight() == null) {
            throw new IllegalStateException("User weight is required for calorie calculation");
        }
        if (user.getHeight() == null) {
            throw new IllegalStateException("User height is required for calorie calculation");
        }
        if (user.getBirthDate() == null) {
            throw new IllegalStateException("User birth date is required for calorie calculation");
        }
        if (user.getGender() == null || user.getGender().trim().isEmpty()) {
            throw new IllegalStateException("User gender is required for calorie calculation");
        }
        if (user.getGoalType() == null) {
            throw new IllegalStateException("User goal type is required for calorie calculation");
        }
    }

    /**
     * Calculate age from birthdate.
     */
    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Internal data classes
    private record GoalAdjustment(double adjustedCalories, double adjustment) {}
    private record SafetyAdjustment(double finalCalories, boolean wasAdjustedForMinimum,
                                    boolean wasAdjustedForMaxDeficit) {}
}
