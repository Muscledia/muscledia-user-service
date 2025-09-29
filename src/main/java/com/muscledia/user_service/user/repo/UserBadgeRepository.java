package com.muscledia.user_service.user.repo;

import com.muscledia.user_service.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser_UserId(Long userId);

    Optional<UserBadge> findByUser_UserIdAndBadgeId(Long userId, Long badgeId);

}
