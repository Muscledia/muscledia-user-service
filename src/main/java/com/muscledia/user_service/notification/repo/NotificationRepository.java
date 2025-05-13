package com.muscledia.user_service.notification.repo;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUser_UserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead);

    long countByUser_UserIdAndIsReadIsFalse(Long userId);
}
