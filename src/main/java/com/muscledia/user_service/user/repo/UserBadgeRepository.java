package com.muscledia.user_service.user.repo;

import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(User user);

    boolean existsByUserAndBadgeId(User user, Long badgeId);
}
