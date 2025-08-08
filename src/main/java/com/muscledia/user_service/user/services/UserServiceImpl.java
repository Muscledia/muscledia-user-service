package com.muscledia.user_service.user.services;

import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UuidToLongIdGenerator uuidGenerator;

    @Override
    @Transactional
    public User saveUser(User user) {
        // Handle new user creation
        if (user.getUserId() == null) {
            // Generate UUID-based ID for new users
            UuidToLongIdGenerator.UuidLongPair idPair = uuidGenerator.generateUniqueId();
            user.setUserId(idPair.getLongId());
            user.setUuidString(idPair.getUuidString());

            // Encode password for new users
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

            // Assign default ROLE_USER to new users
            roleService.addRoleToUser(user, ERole.ROLE_USER);
        } else {
            // For existing users (updates), don't change UUID or regenerate ID
            // Just make sure we don't accidentally overwrite the UUID
            if (user.getUuidString() == null) {
                // This shouldn't happen, but as a safety measure, find existing UUID
                Optional<String> existingUuid = userRepository.findUuidStringByUserId(user.getUserId());
                if (existingUuid.isPresent()) {
                    user.setUuidString(existingUuid.get());
                } else {
                    throw new IllegalStateException("Existing user missing UUID: " + user.getUserId());
                }
            }
        }

        return userRepository.save(user);
    }

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

    /**
     * Get user by UUID string
     */
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

    /**
     * Check if user exists by UUID string
     */
    public boolean existsByUuid(String uuidString) {
        return userRepository.existsByUuidString(uuidString);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Delete user by UUID string
     */
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

    /**
     * Get all users (for admin purposes)
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update existing user
     */
    @Transactional
    public User updateUser(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update operation");
        }

        // Verify the user exists
        if (!userRepository.existsById(user.getUserId())) {
            throw new ResourceNotFoundException("User not found with id: " + user.getUserId());
        }

        return userRepository.save(user);
    }

    /**
     * Convert UUID string to Long ID
     */
    @Transactional(readOnly = true)
    public Long convertUuidToLongId(String uuidString) {
        return userRepository.findUserIdByUuidString(uuidString)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuidString));
    }

    /**
     * Convert Long ID to UUID string
     */
    @Transactional(readOnly = true)
    public String convertLongIdToUuid(Long userId) {
        return userRepository.findUuidStringByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Find user by either Long ID or UUID string (flexible search)
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByIdOrUuid(String identifier) {
        // Try to parse as Long first
        try {
            Long userId = Long.parseLong(identifier);
            return userRepository.findById(userId);
        } catch (NumberFormatException e) {
            // If not a Long, try as UUID string
            if (uuidGenerator.isValidUuid(identifier)) {
                return userRepository.findByUuidString(identifier);
            }
            return Optional.empty();
        }
    }

    /**
     * Get system statistics
     */
    @Transactional(readOnly = true)
    public UserStatistics getSystemStatistics() {
        long totalUsers = userRepository.count();
        return new UserStatistics(totalUsers);
    }

    /**
     * Simple statistics class
     */
    public static class UserStatistics {
        private final long totalUsers;

        public UserStatistics(long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public long getTotalUsers() {
            return totalUsers;
        }
    }
}
