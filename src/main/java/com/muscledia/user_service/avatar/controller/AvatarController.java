package com.muscledia.user_service.avatar.controller;

import com.muscledia.user_service.avatar.dto.SetFlameAnimationRequest;
import com.muscledia.user_service.avatar.dto.UnlockAbilityRequest;
import com.muscledia.user_service.avatar.dto.UpdateAvatarExpRequest;
import com.muscledia.user_service.avatar.dto.UpdateAvatarLevelRequest;
import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import com.muscledia.user_service.avatar.service.IAvatarService;
import com.muscledia.user_service.exception.ResourceNotFoundException;
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
public class AvatarController {
    private final IAvatarService avatarService;

    public AvatarController(@Qualifier("avatarServiceImpl") IAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping("/users/{userId}/avatars")
    public ResponseEntity<Avatar> createAvatar(
            @PathVariable Long userId,
            @NotNull(message = "Avatar type is required") @RequestParam AvatarType avatarType) {
        Avatar newAvatar = avatarService.createAvatar(userId, avatarType);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAvatar);
    }

    @GetMapping("/users/{userId}/avatar") 
                                          // 
    public ResponseEntity<Avatar> getPrimaryAvatar(@PathVariable Long userId) {
        return ResponseEntity.ok(avatarService.getAvatarByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No avatar found for user: " + userId)));
    }

    @GetMapping("/users/{userId}/avatars")
    public ResponseEntity<List<Avatar>> getAllAvatarsForUser(@PathVariable Long userId) {
        List<Avatar> avatars = avatarService.getAllAvatarsForUser(userId);
        return ResponseEntity.ok(avatars);
    }

    @GetMapping("/avatars/{avatarId}")
    public ResponseEntity<Avatar> getAvatarById(@PathVariable Long avatarId) {
        return ResponseEntity.ok(avatarService.getAvatarById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found with id: " + avatarId)));
    }

    @PatchMapping("/avatars/{avatarId}/level")
    public ResponseEntity<Avatar> updateAvatarLevel(
            @PathVariable Long avatarId,
            @Valid @RequestBody UpdateAvatarLevelRequest updateRequest) {
        Avatar updatedAvatar = avatarService.updateAvatarLevel(avatarId, updateRequest.getNewLevel());
        return ResponseEntity.ok(updatedAvatar);
    }

    @PatchMapping("/avatars/{avatarId}/exp")
    public ResponseEntity<Avatar> updateAvatarExp(
            @PathVariable Long avatarId,
            @Valid @RequestBody UpdateAvatarExpRequest updateRequest) {
        Avatar updatedAvatar = avatarService.updateAvatarExp(avatarId, updateRequest.getNewExp());
        return ResponseEntity.ok(updatedAvatar);
    }

    @PatchMapping("/avatars/{avatarId}/ability")
    public ResponseEntity<Avatar> unlockAvatarAbility(
            @PathVariable Long avatarId,
                
            @Valid @RequestBody UnlockAbilityRequest unlockRequest) {
        Avatar updatedAvatar = avatarService.unlockAbility(
                avatarId, 
                unlockRequest.getAbilityKey(), 
                unlockRequest.getAbilityValue()
        );
        return ResponseEntity.ok(updatedAvatar);
    }

    @PatchMapping("/avatars/{avatarId}/flame")
    public ResponseEntity<Avatar> setFlameAnimation(
            @PathVariable Long avatarId,
            @Valid @RequestBody SetFlameAnimationRequest flameRequest) {
        Avatar updatedAvatar = avatarService.setFlameAnimation(avatarId, flameRequest.isEnabled());
        return ResponseEntity.ok(updatedAvatar);
    }

    @DeleteMapping("/avatars/{avatarId}")
    public ResponseEntity<Void> deleteAvatar(@PathVariable Long avatarId) {
        avatarService.deleteAvatar(avatarId);
        return ResponseEntity.noContent().build();
    }
}
