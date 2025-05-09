package com.muscledia.user_service.user.repo;

import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.entity.UserChampion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChampionRepository extends JpaRepository<UserChampion, Long> {
    Optional<UserChampion> findByUserIdAndChampionId(Long userId, Long championId);

    List<UserChampion> findByUserId(Long userId);
}
