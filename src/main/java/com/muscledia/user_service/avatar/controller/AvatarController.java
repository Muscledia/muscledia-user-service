package com.muscledia.user_service.avatar.controller;

import com.muscledia.user_service.avatar.dto.SetFlameAnimationRequest;
import com.muscledia.user_service.avatar.dto.UnlockAbilityRequest;
import com.muscledia.user_service.avatar.dto.UpdateAvatarExpRequest;
import com.muscledia.user_service.avatar.dto.UpdateAvatarLevelRequest;
import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import com.muscledia.user_service.avatar.service.IAvatarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AvatarController {
    private final IAvatarService avatarService;

    public AvatarController(@Qualifier("avatarServiceImpl") IAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping("/users/{userId}/avatars")
    public ResponseEntity<Avatar> createAvatar(
            @PathVariable Long userId,
            @RequestParam AvatarType avatarType) {
        Avatar newAvatar = avatarService.createAvatar(userId, avatarType);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAvatar);
    }

    @GetMapping("/users/{userId}/avatar")
    public ResponseEntity<Optional<Avatar>> getPrimaryAvatar(@PathVariable Long userId) {
        Optional<Avatar> avatar = avatarService.getAvatarByUserId(userId);
        return ResponseEntity.ok(avatar); // Returns 200 even if empty.  Use .orElse(ResponseEntity.notFound().build()) for a 404 if desired
    }

    @GetMapping("/users/{userId}/avatars")
    public ResponseEntity<List<Avatar>> getAllAvatarsForUser(@PathVariable Long userId) {
        List<Avatar> avatars = avatarService.getAllAvatarsForUser(userId);
        return ResponseEntity.ok(avatars);
    }

    @GetMapping("/avatars/{avatarId}")
    public ResponseEntity<Avatar> getAvatarById(@PathVariable Long avatarId) {
        Optional<Avatar> avatar = avatarService.getAvatarById(avatarId);
        return avatar.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
        Avatar updatedAvatar = avatarService.unlockAbility(avatarId, unlockRequest.getAbilityKey(), unlockRequest.getAbilityValue());
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
