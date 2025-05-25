package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.user.entity.UserBadge;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.services.IUserBadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/badges")
@Tag(name = "User Badges", description = "User badge management APIs")
@SecurityRequirement(name = "JWT")
public class UserBadgeController {
        private final IUserBadgeService userBadgeService;

        public UserBadgeController(@Qualifier("userBadgeServiceImpl") IUserBadgeService userBadgeService) {
                this.userBadgeService = userBadgeService;
        }

        @Operation(summary = "Get user's badges", description = "Retrieves all badges for a specific user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Badges retrieved successfully")
        })
        @GetMapping
        public ResponseEntity<List<UserBadge>> getUserBadges(@PathVariable Long userId) {
                List<UserBadge> badges = userBadgeService.getUserBadgesByUserId(userId);
                return ResponseEntity.ok(badges);
        }

        @Operation(summary = "Get specific user badge", description = "Retrieves a specific badge for a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Badge found"),
                        @ApiResponse(responseCode = "404", description = "Badge not found for user")
        })
        @GetMapping("/{badgeId}")
        public ResponseEntity<UserBadge> getUserBadge(
                        @Parameter(description = "ID of the user") @PathVariable Long userId,
                        @Parameter(description = "ID of the badge") @PathVariable Long badgeId) {
                return ResponseEntity.ok(userBadgeService.getUserBadge(userId, badgeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                String.format("Badge %d not found for user %d", badgeId, userId))));
        }

        @Operation(summary = "Award badge to user", description = "Awards a specific badge to a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Badge awarded successfully"),
                        @ApiResponse(responseCode = "404", description = "User or badge not found")
        })
        @PostMapping("/{badgeId}")
        public ResponseEntity<Void> awardBadge(
                        @Parameter(description = "ID of the user") @PathVariable Long userId,
                        @Parameter(description = "ID of the badge to award") @PathVariable Long badgeId) {
                userBadgeService.awardBadge(userId, badgeId);
                return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        @Operation(summary = "Update badge progress", description = "Updates the progress of a specific badge for a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Progress updated successfully"),
                        @ApiResponse(responseCode = "404", description = "User badge not found")
        })
        @PatchMapping("/{badgeId}/progress")
        public ResponseEntity<Void> updateBadgeProgress(
                        @Parameter(description = "ID of the user") @PathVariable Long userId,
                        @Parameter(description = "ID of the badge") @PathVariable Long badgeId,
                        @Parameter(description = "New progress value") @RequestParam int progress) {
                userBadgeService.updateProgress(userId, badgeId, progress);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Save user badge", description = "Creates or updates a user badge")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Badge saved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid badge data")
        })
        @PostMapping
        public ResponseEntity<UserBadge> saveUserBadge(@RequestBody UserBadge userBadge) {
                UserBadge savedBadge = userBadgeService.saveUserBadge(userBadge);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedBadge);
        }
}
