package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.user.entity.UserBadge;
import com.muscledia.user_service.user.services.IUserBadgeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/{userId}/badges")
public class UserBadgeController {
    private final IUserBadgeService userBadgeService;

    public UserBadgeController(@Qualifier("userBadgeServiceImpl") IUserBadgeService userBadgeService) {
        this.userBadgeService = userBadgeService;
    }

    @GetMapping
    public ResponseEntity<List<UserBadge>> getUserBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(userBadgeService.getUserBadgesByUserId(userId));
    }

    @GetMapping("/{badgeId}")
    public ResponseEntity<UserBadge> getUserBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId
    ) {
        Optional<UserBadge> userBadge = userBadgeService.getUserBadge(userId, badgeId);
        return userBadge.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Endpoint to award a badge to a user
    @PostMapping("/{badgeId}")
    public ResponseEntity<Void> awardBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId
    ) {
        userBadgeService.awardBadge(userId, badgeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Endpoint to update the progress of a user's badge
    @PatchMapping("/{badgeId}/progress")
    public ResponseEntity<Void> updateBadgeProgress(
            @PathVariable Long userId,
            @PathVariable Long badgeId,
            @RequestParam int progress // Or @RequestBody DTO if more complex
    ) {
        userBadgeService.updateProgress(userId, badgeId, progress);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserBadge> saveUserBadge(@RequestBody UserBadge userBadge) {
        UserBadge savedBadge = userBadgeService.saveUserBadge(userBadge);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBadge);
    }

}

