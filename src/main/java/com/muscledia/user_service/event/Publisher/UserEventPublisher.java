package com.muscledia.user_service.event.Publisher;

import com.muscledia.user_service.event.UserRegisteredEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
        * Service for publishing user-related events to Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "kafka.events.enabled", havingValue = "true", matchIfMissing = false)
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    @PostConstruct
    public void init() {
        log.info("🚀 UserEventPublisher initialized");
        log.info("📡 Kafka template: {}", kafkaTemplate != null ? "Available" : "NULL");
        log.info("📋 User events topic: {}", userEventsTopic);
    }

    /**
     * Publish user registration event
     */
    public void publishUserRegisteredEvent(Long userId, String username, String email,
                                           Instant registrationDate, String goalType,
                                           String initialAvatarType, Map<String, Object> userPreferences) {
        log.info("📨 publishUserRegisteredEvent called for user {} ({})", userId, username);

        try {
            // Create the event
            log.info("🔧 Creating UserRegisteredEvent...");
            UserRegisteredEvent event = UserRegisteredEvent.create(
                    userId, username, email, registrationDate,
                    goalType, initialAvatarType, userPreferences
            );

            log.info("✅ UserRegisteredEvent created: {}", event);

            // Validate the event
            if (!event.isValid()) {
                log.error("Invalid UserRegisteredEvent created for user {}: {}", userId, event);
                return;
            }

            log.info("✅ Event validation passed");

            // Use userId as the partition key
            String partitionKey = "user-" + userId;
            log.info("🔑 Using partition key: {}", partitionKey);

            log.info("📤 Publishing UserRegisteredEvent for user {} ({}) to topic: {}",
                    userId, username, userEventsTopic);

            // Send the event asynchronously
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userEventsTopic, partitionKey, event);

            // Handle the result
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published UserRegisteredEvent for user {} to partition {} offset {}",
                            userId, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish UserRegisteredEvent for user {}: {}",
                            userId, exception.getMessage(), exception);
                }
            });

        } catch (Exception e) {
            log.error("Unexpected error publishing UserRegisteredEvent for user {}: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * Publish user profile updated event (optional - for future use)
     */
    public void publishUserProfileUpdatedEvent(Long userId, String username, Map<String, Object> changes) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", "USER_PROFILE_UPDATED",
                    "userId", userId,
                    "username", username,
                    "changes", changes,
                    "timestamp", Instant.now()
            );

            String partitionKey = "user-" + userId;

            log.info("Publishing UserProfileUpdatedEvent for user {}", userId);

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userEventsTopic, partitionKey, event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published UserProfileUpdatedEvent for user {}", userId);
                } else {
                    log.error("Failed to publish UserProfileUpdatedEvent for user {}: {}",
                            userId, exception.getMessage(), exception);
                }
            });

        } catch (Exception e) {
            log.error("Unexpected error publishing UserProfileUpdatedEvent for user {}: {}",
                    userId, e.getMessage(), e);
        }
    }
}
