package com.muscledia.user_service.notification.controllers;

import com.muscledia.user_service.notification.entity.Notification;
import com.muscledia.user_service.notification.entity.NotificationType;
import com.muscledia.user_service.notification.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Notifications", description = "Notification management APIs")
@SecurityRequirement(name = "JWT")
public class NotificationController {
    private final INotificationService notificationService;

    public NotificationController(@Qualifier("notificationServiceImpl") INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get user notifications", description = "Retrieves all notifications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get notification by ID", description = "Retrieves a specific notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification found"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(
            @Parameter(description = "ID of the notification") @PathVariable Long notificationId) {
        Notification notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(notification);
    }

    @Operation(summary = "Get unread notifications", description = "Retrieves all unread notifications for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully")
    })
    @GetMapping("/users/{userId}/notifications/unread")
    public ResponseEntity<List<Notification>> getUserUnreadNotifications(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        List<Notification> unreadNotifications = notificationService.getUnreadUserNotifications(userId);
        return ResponseEntity.ok(unreadNotifications);
    }

    @Operation(summary = "Get unread notifications count", description = "Gets the count of unread notifications for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/users/{userId}/notifications/unread/count")
    public ResponseEntity<Long> getUserUnreadNotificationsCount(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(
            @Parameter(description = "ID of the notification") @PathVariable Long notificationId) {
        Notification updatedNotification = notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(updatedNotification);
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications for a user as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    })
    @PatchMapping("/users/{userId}/notifications/read-all")
    public ResponseEntity<Void> markAllUserNotificationsAsRead(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        notificationService.markAllUserNotificationsAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create notification", description = "Creates a new notification for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/users/{userId}/notifications")
    public ResponseEntity<Notification> createNotification(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "Type of the notification") @NotNull(message = "Notification type is required") @RequestParam NotificationType type,
            @Parameter(description = "Notification message") @NotBlank(message = "Message is required") @RequestParam String message) {
        Notification newNotification = notificationService.createNotification(userId, type, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(newNotification);
    }

    @Operation(summary = "Delete notification", description = "Deletes a specific notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "ID of the notification") @PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
