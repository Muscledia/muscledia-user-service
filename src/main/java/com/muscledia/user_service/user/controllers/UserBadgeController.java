package com.muscledia.user_service.user.controllers;

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
                return ResponseEntity.ok(userBadgeService.getUserBadgesByUserId(userId));
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
                return ResponseEntity.ok(userBadgeService.getUserBadge(userId, badgeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                String.format("Badge %d not found for user %d", badgeId, userId))));
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
        public ResponseEntity<Void> awardBadge(
                        @Parameter(description = "ID of the user") @PathVariable Long userId,
                        @Parameter(description = "ID of the badge to award") @PathVariable Long badgeId) {
                userBadgeService.awardBadge(userId, badgeId);
                return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        @IsAdmin
        @Operation(summary = "Update badge progress", description = "Updates the progress of a user's badge")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Progress updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Badge not found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PatchMapping("/{badgeId}/progress")
        public ResponseEntity<Void> updateBadgeProgress(
                        @Parameter(description = "ID of the user") @PathVariable Long userId,
                        @Parameter(description = "ID of the badge") @PathVariable Long badgeId,
                        @Parameter(description = "New progress value") @RequestParam int progress) {
                userBadgeService.updateProgress(userId, badgeId, progress);
                return ResponseEntity.noContent().build();
        }

        @IsAdmin
        @Operation(summary = "Save user badge", description = "Creates or updates a user badge")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Badge saved successfully"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
        })
        @PostMapping
        public ResponseEntity<UserBadge> saveUserBadge(@RequestBody UserBadge userBadge) {
                UserBadge savedBadge = userBadgeService.saveUserBadge(userBadge);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedBadge);
        }
}
