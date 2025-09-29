package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.entity.UserChampion;
import com.muscledia.user_service.user.repo.UserChampionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserChampionServiceImpl implements IUserChampionService {

    private final UserChampionRepository userChampionRepository;
    private final IUserService userService; // Inject UserService

    @Override
    @Transactional
    public UserChampion saveUserChampion(UserChampion userChampion) {
        return userChampionRepository.save(userChampion);
    }

    @Override
    public Optional<UserChampion> getUserChampion(Long userId, Long championId) {
        return userChampionRepository.findByUser_UserIdAndChampionId(userId, championId);
    }

    @Override
    public List<UserChampion> getUserChampionsByUserId(Long userId) {
        return userChampionRepository.findByUser_UserId(userId);
    }

    @Override
    @Transactional
    public void updateExerciseCount(Long userId, Long championId, int count) {
        UserChampion userChampion = getUserChampion(userId, championId)
                .orElseThrow(() -> new RuntimeException("UserChampion not found")); // Handle not found
        userChampion.setCurrentExerciseCount(count);
        userChampionRepository.save(userChampion);
    }

    @Override
    @Transactional
    public void markChampionDefeated(Long userId, Long championId) {
        UserChampion userChampion = getUserChampion(userId, championId)
                .orElseThrow(() -> new RuntimeException("UserChampion not found"));  // Handle not found
        userChampion.setDefeated(true);
        userChampion.setDefeatedDate(LocalDateTime.now());
        userChampionRepository.save(userChampion);
    }

    @Override
    @Transactional
    public UserChampion startBattle(Long userId, Long championId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<UserChampion> existingBattle = getUserChampion(userId, championId);

        if (existingBattle.isPresent()) {
            return existingBattle.get(); // Or throw an exception
        }
        UserChampion userChampion = new UserChampion();
        userChampion.setUser(user);
        userChampion.setChampionId(championId);
        return saveUserChampion(userChampion);

    }
}
