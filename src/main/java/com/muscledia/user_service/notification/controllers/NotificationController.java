package com.muscledia.user_service.notification.controllers;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.notification.entity.NotificationType;
import com.muscledia.user_service.notification.service.INotificationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class NotificationController {
    private final INotificationService notificationService;

    public NotificationController(@Qualifier("notificationServiceImpl") INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long notificationId) {
        Notification notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/users/{userId}/notifications/unread")
    public ResponseEntity<List<Notification>> getUserUnreadNotifications(@PathVariable Long userId) {
        List<Notification> unreadNotifications = notificationService.getUnreadUserNotifications(userId);
        return ResponseEntity.ok(unreadNotifications);
    }

    @GetMapping("/users/{userId}/notifications/unread/count")
    public ResponseEntity<Long> getUserUnreadNotificationsCount(@PathVariable Long userId) {
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(@PathVariable Long notificationId) {
        Notification updatedNotification = notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(updatedNotification);
    }

    @PatchMapping("/users/{userId}/notifications/read-all")
    public ResponseEntity<Void> markAllUserNotificationsAsRead(@PathVariable Long userId) {
        notificationService.markAllUserNotificationsAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/notifications")
    public ResponseEntity<Notification> createNotification(
            @PathVariable Long userId,
            @NotNull(message = "Notification type is required") @RequestParam NotificationType type,
            @NotBlank(message = "Message is required") @RequestParam String message) {
        Notification newNotification = notificationService.createNotification(userId, type, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(newNotification);
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
