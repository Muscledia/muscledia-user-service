package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.entity.UserBadge;
import com.muscledia.user_service.user.exception.BadgeAlreadyAwardedException;
import com.muscledia.user_service.user.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.repo.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBadgeServiceImpl implements IUserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final IUserService userService;

    @Override
    @Transactional
    public UserBadge saveUserBadge(UserBadge userBadge) {
        return userBadgeRepository.save(userBadge);
    }

    @Override
    public Optional<UserBadge> getUserBadge(Long userId, Long badgeId) {
        return userBadgeRepository.findByUser_UserIdAndBadgeId(userId, badgeId);
    }

    @Override
    public List<UserBadge> getUserBadgesByUserId(Long userId) {
        // Verify user exists
        userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userBadgeRepository.findByUser_UserId(userId);
    }

    @Override
    @Transactional
    public void updateProgress(Long userId, Long badgeId, int progress) {
        UserBadge userBadge = getUserBadge(userId, badgeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Badge %d not found for user %d", badgeId, userId)));
        userBadge.setProgress(progress);
        userBadgeRepository.save(userBadge);
    }

    @Override
    @Transactional
    public void awardBadge(Long userId, Long badgeId) {
        // Check if badge is already awarded
        if (getUserBadge(userId, badgeId).isPresent()) {
            throw new BadgeAlreadyAwardedException(
                    String.format("Badge %d is already awarded to user %d", badgeId, userId));
        }

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserBadge newUserBadge = new UserBadge();
        newUserBadge.setUser(user);
        newUserBadge.setBadgeId(badgeId);
        saveUserBadge(newUserBadge);
    }
}
