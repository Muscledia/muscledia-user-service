package com.muscledia.user_service.notification.controllers;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.notification.entity.NotificationType;
import com.muscledia.user_service.notification.service.INotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {
    private final INotificationService notificationService;

    public NotificationController(@Qualifier("notificationServiceImpl") INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long notificationId  ) {
        return ResponseEntity.ok(notificationService.getNotificationById(notificationId));
    }

    @GetMapping("/users/{userId}/notifications/unread")
    public ResponseEntity<List<Notification>> getUserUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadUserNotifications(userId));
    }

    @GetMapping("/users/{userId}/notifications/unread/count")
    public ResponseEntity<Long> getUserUnreadNotificationsCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnreadNotifications(userId));
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
            @RequestParam NotificationType type,
            @RequestParam String message
    ) {
        Notification newNotification = notificationService.createNotification(userId, type, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(newNotification);
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

}
