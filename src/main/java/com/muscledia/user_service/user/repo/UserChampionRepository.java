package com.muscledia.user_service.user.repo;

import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.entity.UserChampion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChampionRepository extends JpaRepository<UserChampion, Long> {
    List<UserChampion> findByUser(User user);

    boolean existsByUser(User user);
}
