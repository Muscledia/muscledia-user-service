package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.User;

import java.util.Optional;

public interface IUserService {
    Optional<User> getUserById(Long userId);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User saveUser(User user);
}
