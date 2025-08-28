package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.exception.BadgeAlreadyAwardedException;
import com.muscledia.user_service.user.entity.UserBadge;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.services.IUserBadgeService;
import com.muscledia.user_service.security.annotation.IsAdmin;
import com.muscledia.user_service.security.annotation.IsUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/badges")
@Tag(name = "User Badges", description = "User badge management APIs")
public class UserBadgeController {
        private final IUserBadgeService userBadgeService;


        public UserBadgeController(@Qualifier("userBadgeServiceImpl") IUserBadgeService userBadgeService) {
                this.userBadgeService = userBadgeService;
        }

        @IsUser
        @Operation(summary = "Get user badges", description = "Retrieves all badges for a user")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Badges retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER or ADMIN role")
        })
        @GetMapping
        public ResponseEntity<List<UserBadge>> getUserBadges(@PathVariable Long userId) {
                List<UserBadge> badges = userBadgeService.getUserBadgesByUserId(userId);
                return ResponseEntity.ok(badges);
        }

        @IsUser
        @Operation(summary = "Get specific badge", description = "Retrieves a specific badge for a user")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Badge found"),
                @ApiResponse(responseCode = "404", description = "Badge not found"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER or ADMIN role")
        })
        @GetMapping("/{badgeId}")
        public ResponseEntity<UserBadge> getUserBadge(
                @Parameter(description = "ID of the user") @PathVariable Long userId,
                @Parameter(description = "ID of the badge") @PathVariable Long badgeId) {

                UserBadge badge = userBadgeService.getUserBadge(userId, badgeId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Badge %d not found for user %d", badgeId, userId)));

                return ResponseEntity.ok(badge);
        }

        @IsAdmin
        @Operation(summary = "Award badge", description = "Awards a badge to a user")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Badge awarded successfully"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "409", description = "Badge already awarded"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping("/{badgeId}")
        public ResponseEntity<?> awardBadge(
                @Parameter(description = "ID of the user") @PathVariable Long userId,
                @Parameter(description = "ID of the badge to award") @PathVariable Long badgeId) {

                try {
                        userBadgeService.awardBadge(userId, badgeId);
                        return ResponseEntity.status(HttpStatus.CREATED).build();
                } catch (BadgeAlreadyAwardedException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("error", e.getMessage()));
                }
        }

        @IsAdmin
        @Operation(summary = "Update badge progress", description = "Updates the progress of a user's badge")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "Progress updated successfully"),
                @ApiResponse(responseCode = "404", description = "Badge not found"),
                @ApiResponse(responseCode = "400", description = "Invalid progress value"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PatchMapping("/{badgeId}/progress")
        public ResponseEntity<?> updateBadgeProgress(
                @Parameter(description = "ID of the user") @PathVariable Long userId,
                @Parameter(description = "ID of the badge") @PathVariable Long badgeId,
                @Parameter(description = "New progress value") @RequestParam int progress) {

                try {
                        if (progress < 0) {
                                return ResponseEntity.badRequest()
                                        .body(Map.of("error", "Progress cannot be negative"));
                        }

                        userBadgeService.updateProgress(userId, badgeId, progress);
                        return ResponseEntity.noContent().build();
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", e.getMessage()));
                }
        }

        @IsAdmin
        @Operation(summary = "Save user badge", description = "Creates or updates a user badge")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Badge saved successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid badge data"),
                @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping
        public ResponseEntity<?> saveUserBadge(
                @PathVariable Long userId,
                @RequestBody UserBadge userBadge) {

                try {
                        // Ensure the badge belongs to the correct user
                        if (userBadge.getUser() == null || !userBadge.getUser().getUserId().equals(userId)) {
                                return ResponseEntity.badRequest()
                                        .body(Map.of("error", "Badge must belong to the specified user"));
                        }

                        UserBadge savedBadge = userBadgeService.saveUserBadge(userBadge);
                        return ResponseEntity.status(HttpStatus.CREATED).body(savedBadge);
                } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Failed to save badge: " + e.getMessage()));
                }
        }
}
