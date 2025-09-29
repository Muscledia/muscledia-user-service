package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.UserBadge;

import java.util.List;
import java.util.Optional;

public interface IUserBadgeService {
    UserBadge saveUserBadge(UserBadge userBadge);

    Optional<UserBadge> getUserBadge(Long userId, Long badgeId);

    List<UserBadge> getUserBadgesByUserId(Long userId);

    void updateProgress(Long userId, Long badgeId, int progress);

    void awardBadge(Long userId, Long badgeId); // Simplified awardBadge
}
