package com.muscledia.user_service.notification.service;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.notification.entity.NotificationType;

import java.util.List;

public interface INotificationService {
    Notification createNotification(Long userId, NotificationType type, String message);

    Notification getNotificationById(Long notificationId);

    List<Notification> getUserNotifications(Long userId);

    List<Notification> getUnreadUserNotifications(Long userId);

    long countUnreadNotifications(Long userId);

    Notification markNotificationAsRead(Long notificationId);

    void markAllUserNotificationsAsRead(Long userId);

    void deleteNotification(Long notificationId);
}
