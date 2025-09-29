package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.UserChampion;

import java.util.List;
import java.util.Optional;

public interface IUserChampionService {

    UserChampion saveUserChampion(UserChampion userChampion);

    Optional<UserChampion> getUserChampion(Long userId, Long championId);

    List<UserChampion> getUserChampionsByUserId(Long userId);

    void updateExerciseCount(Long userId, Long championId, int count);

    void markChampionDefeated(Long userId, Long championId);

    UserChampion startBattle(Long userId, Long championId);
}
