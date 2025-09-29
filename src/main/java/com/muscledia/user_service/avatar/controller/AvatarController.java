package com.muscledia.user_service.avatar.controller;

import com.muscledia.user_service.avatar.dto.SetFlameAnimationRequest;
import com.muscledia.user_service.avatar.dto.UnlockAbilityRequest;
import com.muscledia.user_service.avatar.dto.UpdateAvatarExpRequest;
import com.muscledia.user_service.avatar.dto.UpdateAvatarLevelRequest;
import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import com.muscledia.user_service.avatar.service.IAvatarService;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Avatars", description = "Avatar management APIs")
@SecurityRequirement(name = "JWT")
public class AvatarController {
    private final IAvatarService avatarService;

    public AvatarController(@Qualifier("avatarServiceImpl") IAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @Operation(summary = "Create avatar", description = "Creates a new avatar for a user with specified avatar type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Avatar created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid avatar type"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/users/{userId}/avatars")
    public ResponseEntity<Avatar> createAvatar(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "Type of avatar to create") @NotNull(message = "Avatar type is required") @RequestParam AvatarType avatarType) {
        Avatar newAvatar = avatarService.createAvatar(userId, avatarType);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAvatar);
    }

    @Operation(summary = "Get user's primary avatar", description = "Retrieves the primary avatar for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar found"),
            @ApiResponse(responseCode = "404", description = "No avatar found for user")
    })
    @GetMapping("/users/{userId}/avatar")
    public ResponseEntity<Avatar> getPrimaryAvatar(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        return ResponseEntity.ok(avatarService.getAvatarByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No avatar found for user: " + userId)));
    }

    @Operation(summary = "Get all user avatars", description = "Retrieves all avatars associated with a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatars retrieved successfully")
    })
    @GetMapping("/users/{userId}/avatars")
    public ResponseEntity<List<Avatar>> getAllAvatarsForUser(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        List<Avatar> avatars = avatarService.getAllAvatarsForUser(userId);
        return ResponseEntity.ok(avatars);
    }

    @Operation(summary = "Get avatar by ID", description = "Retrieves a specific avatar by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar found"),
            @ApiResponse(responseCode = "404", description = "Avatar not found")
    })
    @GetMapping("/avatars/{avatarId}")
    public ResponseEntity<Avatar> getAvatarById(
            @Parameter(description = "ID of the avatar") @PathVariable Long avatarId) {
        return ResponseEntity.ok(avatarService.getAvatarById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found with id: " + avatarId)));
    }

    @Operation(summary = "Update avatar level", description = "Updates the level of a specific avatar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar level updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid level value"),
            @ApiResponse(responseCode = "404", description = "Avatar not found")
    })
    @PatchMapping("/avatars/{avatarId}/level")
    public ResponseEntity<Avatar> updateAvatarLevel(
            @Parameter(description = "ID of the avatar") @PathVariable Long avatarId,
            @Valid @RequestBody UpdateAvatarLevelRequest updateRequest) {
        Avatar updatedAvatar = avatarService.updateAvatarLevel(avatarId, updateRequest.getNewLevel());
        return ResponseEntity.ok(updatedAvatar);
    }

    @Operation(summary = "Update avatar experience", description = "Updates the experience points of a specific avatar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar experience updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid experience value"),
            @ApiResponse(responseCode = "404", description = "Avatar not found")
    })
    @PatchMapping("/avatars/{avatarId}/exp")
    public ResponseEntity<Avatar> updateAvatarExp(
            @Parameter(description = "ID of the avatar") @PathVariable Long avatarId,
            @Valid @RequestBody UpdateAvatarExpRequest updateRequest) {
        Avatar updatedAvatar = avatarService.updateAvatarExp(avatarId, updateRequest.getNewExp());
        return ResponseEntity.ok(updatedAvatar);
    }

    @Operation(summary = "Unlock avatar ability", description = "Unlocks a new ability for a specific avatar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ability unlocked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ability data"),
            @ApiResponse(responseCode = "404", description = "Avatar not found")
    })
    @PatchMapping("/avatars/{avatarId}/ability")
    public ResponseEntity<Avatar> unlockAvatarAbility(
            @Parameter(description = "ID of the avatar") @PathVariable Long avatarId,
            @Valid @RequestBody UnlockAbilityRequest unlockRequest) {
        Avatar updatedAvatar = avatarService.unlockAbility(
                avatarId,
                unlockRequest.getAbilityKey(),
                unlockRequest.getAbilityValue());
        return ResponseEntity.ok(updatedAvatar);
    }

    @Operation(summary = "Set flame animation", description = "Enables or disables the flame animation for a specific avatar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flame animation setting updated successfully"),
            @ApiResponse(responseCode = "404", description = "Avatar not found")
    })
    @PatchMapping("/avatars/{avatarId}/flame")
    public ResponseEntity<Avatar> setFlameAnimation(
            @Parameter(description = "ID of the avatar") @PathVariable Long avatarId,
            @Valid @RequestBody SetFlameAnimationRequest flameRequest) {
        Avatar updatedAvatar = avatarService.setFlameAnimation(avatarId, flameRequest.isEnabled());
        return ResponseEntity.ok(updatedAvatar);
    }

    @Operation(summary = "Delete avatar", description = "Deletes a specific avatar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Avatar deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Avatar not found")
    })
    @DeleteMapping("/avatars/{avatarId}")
    public ResponseEntity<Void> deleteAvatar(
            @Parameter(description = "ID of the avatar") @PathVariable Long avatarId) {
        avatarService.deleteAvatar(avatarId);
        return ResponseEntity.noContent().build();
    }
}
