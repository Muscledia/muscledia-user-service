package com.muscledia.user_service.notification.repo;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);

    List<Notification> findByUserAndIsReadFalse(User user);

    long countByUserAndIsReadFalse(User user);
}
