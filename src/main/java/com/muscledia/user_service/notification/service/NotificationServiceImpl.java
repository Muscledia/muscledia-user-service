package com.muscledia.user_service.notification.service;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.notification.entity.NotificationType;
import com.muscledia.user_service.notification.repo.NotificationRepository;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final IUserService userService;

    @Override
    @Transactional
    public Notification createNotification(Long userId, NotificationType type, String message) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadUserNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUser_UserIdAndIsReadIsFalse(userId);
    }

    @Override
    @Transactional
    public Notification markNotificationAsRead(Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllUserNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = getUserNotifications(userId);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
