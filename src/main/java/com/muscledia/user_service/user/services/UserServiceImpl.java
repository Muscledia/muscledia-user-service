package com.muscledia.user_service.user.services;

import com.muscledia.user_service.event.Publisher.UserEventPublisher;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
//@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UuidToLongIdGenerator uuidGenerator;

    // Optional dependency - only injected if Kafka is enabled
    @Autowired(required = false)
    private UserEventPublisher eventPublisher;

    // ADD THIS: Constructor logging to see if eventPublisher is injected
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           PasswordEncoder passwordEncoder,
                           UuidToLongIdGenerator uuidGenerator) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.uuidGenerator = uuidGenerator;

        log.info("UserServiceImpl initialized");
    }

    // ADD THIS: Method to check eventPublisher status after injection
    @PostConstruct
    public void checkEventPublisher() {
        if (eventPublisher != null) {
            log.info("✅ UserEventPublisher is available - Kafka events enabled");
        } else {
            log.warn("❌ UserEventPublisher is NULL - Kafka events disabled");
        }
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        boolean isNewUser = user.getUserId() == null;

        // Handle new user creation
        if (isNewUser) {
            log.info("Creating new user: {}", user.getUsername());

            // Generate UUID-based ID for new users
            UuidToLongIdGenerator.UuidLongPair idPair = uuidGenerator.generateUniqueId();
            user.setUserId(idPair.getLongId());
            user.setUuidString(idPair.getUuidString());

            // Encode password for new users
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

            // Set creation timestamp (will be handled by @PrePersist)
            // user.setCreatedAt() is handled automatically

            // Assign default ROLE_USER to new users
            roleService.addRoleToUser(user, ERole.ROLE_USER);
        } else {
            // For existing users (updates)
            if (user.getUuidString() == null) {
                Optional<String> existingUuid = userRepository.findUuidStringByUserId(user.getUserId());
                if (existingUuid.isPresent()) {
                    user.setUuidString(existingUuid.get());
                } else {
                    throw new IllegalStateException("Existing user missing UUID: " + user.getUserId());
                }
            }
        }

        // Save the user
        User savedUser = userRepository.save(user);
        log.info("User saved successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getUserId());

        // Publish registration event for new users
        if (isNewUser) {
            publishUserRegistrationEvent(savedUser);
        }

        return savedUser;
    }

    /**
     * Register a new user (convenience method with event publishing)
     */
    @Transactional
    public User registerUser(User user) {
        log.info("Registering new user: {}", user.getUsername());

        // Validate user doesn't already exist
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Save the user (this will trigger event publishing)
        return saveUser(user);
    }

    /**
     * Update existing user with optional event publishing
     */
    @Override
    @Transactional
    public User updateUser(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update operation");
        }

        // Get the existing user to track changes
        User existingUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user.getUserId()));

        // Track changes for event publishing
        Map<String, Object> changes = trackUserChanges(existingUser, user);

        // Save the updated user
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());

        // Publish profile update event if there were significant changes
        if (!changes.isEmpty()) {
            publishUserProfileUpdateEvent(updatedUser, changes);
        }

        return updatedUser;
    }

    // ===========================================
    // KAFKA EVENT PUBLISHING METHODS
    // ===========================================

    /**
     * Publish user registration event to Kafka
     */
    private void publishUserRegistrationEvent(User user) {
        if (eventPublisher != null) {
            try {
                // Convert LocalDateTime to Instant for the event
                Instant registrationDate = convertToInstant(user.getCreatedAt());

                // Prepare user preferences map
                Map<String, Object> userPreferences = buildUserPreferencesMap(user);

                // Get goal type and avatar type as strings
                String goalTypeStr = user.getGoalType() != null ? user.getGoalType().name() : null;
                String avatarTypeStr = (String) user.getInitialAvatarType();

                // Publish the event
                eventPublisher.publishUserRegisteredEvent(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        registrationDate,
                        goalTypeStr,
                        avatarTypeStr,
                        userPreferences
                );

                log.info("Published UserRegisteredEvent for user {} (ID: {})",
                        user.getUsername(), user.getUserId());

            } catch (Exception e) {
                log.error("Failed to publish registration event for user {} (ID: {}): {}",
                        user.getUsername(), user.getUserId(), e.getMessage());
                // Don't fail the registration if event publishing fails
            }
        } else {
            log.debug("Kafka event publishing is disabled, skipping UserRegisteredEvent for user {}",
                    user.getUsername());
        }
    }

    /**
     * Publish user profile update event to Kafka
     */
    private void publishUserProfileUpdateEvent(User user, Map<String, Object> changes) {
        if (eventPublisher != null) {
            try {
                eventPublisher.publishUserProfileUpdatedEvent(
                        user.getUserId(),
                        user.getUsername(),
                        changes
                );

                log.info("Published UserProfileUpdatedEvent for user {} (ID: {})",
                        user.getUsername(), user.getUserId());

            } catch (Exception e) {
                log.error("Failed to publish profile update event for user {} (ID: {}): {}",
                        user.getUsername(), user.getUserId(), e.getMessage());
                // Don't fail the update if event publishing fails
            }
        }
    }

    /**
     * Build user preferences map for event publishing
     */
    private Map<String, Object> buildUserPreferencesMap(User user) {
        Map<String, Object> userPreferences = new HashMap<>();

        if (user.getBirthDate() != null) {
            userPreferences.put("birthDate", user.getBirthDate().toString());
        }
        if (user.getGender() != null) {
            userPreferences.put("gender", user.getGender());
        }
        if (user.getHeight() != null) {
            userPreferences.put("height", user.getHeight());
        }
        if (user.getInitialWeight() != null) {
            userPreferences.put("initialWeight", user.getInitialWeight());
        }
        if (user.getCurrentStreak() != null) {
            userPreferences.put("currentStreak", user.getCurrentStreak());
        }
        if (user.getTotalExp() != null) {
            userPreferences.put("totalExp", user.getTotalExp());
        }

        return userPreferences;
    }

    /**
     * Track changes between existing and updated user
     */
    private Map<String, Object> trackUserChanges(User existingUser, User updatedUser) {
        Map<String, Object> changes = new HashMap<>();

        // Track email changes
        if (!Objects.equals(existingUser.getEmail(), updatedUser.getEmail())) {
            changes.put("email", Map.of(
                    "old", existingUser.getEmail() != null ? existingUser.getEmail() : "",
                    "new", updatedUser.getEmail() != null ? updatedUser.getEmail() : ""
            ));
        }

        // Track goal type changes
        if (!Objects.equals(existingUser.getGoalType(), updatedUser.getGoalType())) {
            changes.put("goalType", Map.of(
                    "old", existingUser.getGoalType() != null ? existingUser.getGoalType().name() : "",
                    "new", updatedUser.getGoalType() != null ? updatedUser.getGoalType().name() : ""
            ));
        }

        // Track height changes
        if (!Objects.equals(existingUser.getHeight(), updatedUser.getHeight())) {
            changes.put("height", Map.of(
                    "old", existingUser.getHeight() != null ? existingUser.getHeight() : 0.0,
                    "new", updatedUser.getHeight() != null ? updatedUser.getHeight() : 0.0
            ));
        }

        // Track weight changes
        if (!Objects.equals(existingUser.getInitialWeight(), updatedUser.getInitialWeight())) {
            changes.put("weight", Map.of(
                    "old", existingUser.getInitialWeight() != null ? existingUser.getInitialWeight() : 0.0,
                    "new", updatedUser.getInitialWeight() != null ? updatedUser.getInitialWeight() : 0.0
            ));
        }

        // Track avatar type changes
        if (!Objects.equals(existingUser.getInitialAvatarType(), updatedUser.getInitialAvatarType())) {
            changes.put("avatarType", Map.of(
                    "old", existingUser.getInitialAvatarType() != null ? existingUser.getInitialAvatarType() : "",
                    "new", updatedUser.getInitialAvatarType() != null ? updatedUser.getInitialAvatarType() : ""
            ));
        }

        return changes;
    }

    /**
     * Convert LocalDateTime to Instant for event publishing
     */
    private Instant convertToInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return Instant.now();
        }
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    // ===========================================
    // KEEP ALL YOUR EXISTING METHODS UNCHANGED
    // ===========================================

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUuid(String uuidString) {
        return userRepository.findByUuidString(uuidString);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUuid(String uuidString) {
        return userRepository.existsByUuidString(uuidString);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void deleteUserByUuid(String uuidString) {
        userRepository.deleteByUuidString(uuidString);
    }

    @Transactional
    public User promoteToAdmin(Long userId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        roleService.addRoleToUser(user, ERole.ROLE_ADMIN);
        return userRepository.save(user);
    }

    @Transactional
    public User demoteFromAdmin(Long userId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        roleService.removeRoleFromUser(user, ERole.ROLE_ADMIN);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Long convertUuidToLongId(String uuidString) {
        return userRepository.findUserIdByUuidString(uuidString)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuidString));
    }

    @Transactional(readOnly = true)
    public String convertLongIdToUuid(Long userId) {
        return userRepository.findUuidStringByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByIdOrUuid(String identifier) {
        try {
            Long userId = Long.parseLong(identifier);
            return userRepository.findById(userId);
        } catch (NumberFormatException e) {
            if (uuidGenerator.isValidUuid(identifier)) {
                return userRepository.findByUuidString(identifier);
            }
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public UserStatistics getSystemStatistics() {
        long totalUsers = userRepository.count();
        return new UserStatistics(totalUsers);
    }

    @Getter
    public static class UserStatistics {
        private final long totalUsers;

        public UserStatistics(long totalUsers) {
            this.totalUsers = totalUsers;
        }

    }
}
