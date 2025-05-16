package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.entity.UserBadge;
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
    private final IUserService userService; // Inject UserService

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
        return userBadgeRepository.findByUser_UserId(userId);
    }

    @Override
    @Transactional
    public void updateProgress(Long userId, Long badgeId, int progress) {
        UserBadge userBadge = getUserBadge(userId, badgeId)
                .orElseThrow(() -> new RuntimeException("UserBadge not found")); // Handle not found
        userBadge.setProgress(progress);
        userBadgeRepository.save(userBadge);
    }

    @Override
    @Transactional
    public void awardBadge(Long userId, Long badgeId) {
        Optional<UserBadge> existingBadge = getUserBadge(userId, badgeId);
        if (existingBadge.isPresent()) {
            return; // Or throw an exception:  throw new IllegalStateException("Badge already awarded");
        }

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // Handle not found

        UserBadge newUserBadge = new UserBadge();
        newUserBadge.setUser(user);
        newUserBadge.setBadgeId(badgeId); //  Set the badgeId
        saveUserBadge(newUserBadge);
    }
}
