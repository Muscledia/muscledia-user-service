package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(Long userId);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User saveUser(User user);

    void deleteUser(Long userId);

    List<User> getAllUsers();
    User updateUser(User user);

    // UUID-specific methods
    Optional<User> getUserByUuid(String uuidString);
    boolean existsByUuid(String uuidString);
    void deleteUserByUuid(String uuidString);

    // Conversion utilities
    Long convertUuidToLongId(String uuidString);
    String convertLongIdToUuid(Long userId);
    Optional<User> findUserByIdOrUuid(String identifier);

    // Admin operations
    User promoteToAdmin(Long userId);
    User demoteFromAdmin(Long userId);

    // Statistics
    UserServiceImpl.UserStatistics getSystemStatistics();
}
