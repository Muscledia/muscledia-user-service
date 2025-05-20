package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.user.entity.UserBadge;
import com.muscledia.user_service.user.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.services.IUserBadgeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/badges")
public class UserBadgeController {
    private final IUserBadgeService userBadgeService;

    public UserBadgeController(@Qualifier("userBadgeServiceImpl") IUserBadgeService userBadgeService) {
        this.userBadgeService = userBadgeService;
    }

    @GetMapping
    public ResponseEntity<List<UserBadge>> getUserBadges(@PathVariable Long userId) {
        List<UserBadge> badges = userBadgeService.getUserBadgesByUserId(userId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/{badgeId}")
    public ResponseEntity<UserBadge> getUserBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId) {
        return ResponseEntity.ok(userBadgeService.getUserBadge(userId, badgeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Badge %d not found for user %d", badgeId, userId))));
    }

    @PostMapping("/{badgeId}")
    public ResponseEntity<Void> awardBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId) {
        userBadgeService.awardBadge(userId, badgeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{badgeId}/progress")
    public ResponseEntity<Void> updateBadgeProgress(
            @PathVariable Long userId,
            @PathVariable Long badgeId,
            @RequestParam int progress) {
        userBadgeService.updateProgress(userId, badgeId, progress);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserBadge> saveUserBadge(@RequestBody UserBadge userBadge) {
        UserBadge savedBadge = userBadgeService.saveUserBadge(userBadge);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBadge);
    }
}
