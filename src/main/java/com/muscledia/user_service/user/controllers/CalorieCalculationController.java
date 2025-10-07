package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.user.dto.CalorieCalculationRequest;
import com.muscledia.user_service.user.dto.CalorieCalculationResponse;
import com.muscledia.user_service.user.services.ICalorieCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for daily calorie needs calculation.
 * Provides endpoints for calculating recommended calorie intake based on user profile and goals.
 */
@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
@Tag(name = "Calorie Calculator", description = "Daily calorie needs calculation based on user goals")
@SecurityRequirement(name = "JWT")
public class CalorieCalculationController {

    private final ICalorieCalculationService calorieCalculationService;

    public CalorieCalculationController(@Qualifier("calorieCalculationServiceImpl")ICalorieCalculationService calorieCalculationService) {
        this.calorieCalculationService = calorieCalculationService;
    }

    /**
     * Calculate daily calorie needs based on provided parameters.
     * This endpoint allows calculating calories without referencing a specific user.
     */
    @Operation(
            summary = "Calculate daily calorie needs",
            description = "Calculate recommended daily calorie intake based on current stats, activity level, and fitness goals using Mifflin-St Jeor equation"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Calories calculated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CalorieCalculationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input parameters",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/calculate-calories")
    public ResponseEntity<CalorieCalculationResponse> calculateCalories(
            @Parameter(description = "Calorie calculation parameters", required = true)
            @Valid @RequestBody CalorieCalculationRequest request
    ) {
        try {
            log.info("Received calorie calculation request for goal: {}", request.getGoalType());
            CalorieCalculationResponse response = calorieCalculationService.calculateCalories(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid calculation parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error calculating calories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate calories", e);
        }
    }

    /**
     * Calculate daily calorie needs for a specific user based on their stored profile.
     * This endpoint fetches user data from the database.
     */
    @Operation(
            summary = "Calculate calories for specific user",
            description = "Calculate recommended daily calorie intake for a user based on their stored profile data and goals"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Calories calculated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CalorieCalculationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User profile incomplete - missing required data",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/{userId}/calculate-calories")
    public ResponseEntity<CalorieCalculationResponse> calculateCaloriesForUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId
    ) {
        try {
            log.info("Calculating calories for user ID: {}", userId);
            CalorieCalculationResponse response = calorieCalculationService.calculateCaloriesForUser(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.error("Incomplete user profile for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CalorieCalculationResponse.builder()
                            .notes(e.getMessage())
                            .disclaimer("Please complete your profile to calculate calorie needs.")
                            .build());
        } catch (Exception e) {
            log.error("Error calculating calories for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Health check endpoint for the calorie calculator.
     */
    @Operation(
            summary = "Health check",
            description = "Check if the calorie calculator service is operational"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is healthy",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/calculate-calories/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Calorie Calculator",
                "version", "1.0.0"
        ));
    }

    /**
     * Get information about the calculation method.
     */
    @Operation(
            summary = "Get calculation method info",
            description = "Get information about the Mifflin-St Jeor equation and safety constraints used"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Method information retrieved",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/calculate-calories/method")
    public ResponseEntity<Map<String, Object>> getMethodInfo() {
        return ResponseEntity.ok(Map.of(
                "method", "Mifflin-St Jeor Equation",
                "bmrFormula", Map.of(
                        "men", "BMR = (10 × weight kg) + (6.25 × height cm) - (5 × age) + 5",
                        "women", "BMR = (10 × weight kg) + (6.25 × height cm) - (5 × age) - 161"
                ),
                "tdeeFormula", "TDEE = BMR × Activity Multiplier",
                "activityMultipliers", Map.of(
                        "SEDENTARY", 1.2,
                        "LIGHTLY_ACTIVE", 1.375,
                        "MODERATELY_ACTIVE", 1.55,
                        "VERY_ACTIVE", 1.725,
                        "EXTRA_ACTIVE", 1.9
                ),
                "goalAdjustments", Map.of(
                        "LOSE_WEIGHT", "-500 kcal/day (1 lb/week loss)",
                        "BUILD_STRENGTH", "TDEE (maintenance)",
                        "GAIN_MUSCLE", "+250-500 kcal/day"
                ),
                "safetyConstraints", Map.of(
                        "minCaloriesMale", 1500,
                        "minCaloriesFemale", 1200,
                        "maxDeficit", 1000
                )
        ));
    }
}
