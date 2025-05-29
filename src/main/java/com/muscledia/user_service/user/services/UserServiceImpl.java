package com.muscledia.user_service.user.services;

import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Override
    @Transactional
    public User saveUser(User user) {
        // Encode password if it's a new user
        if (user.getUserId() == null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            // Assign default ROLE_USER to new users
            roleService.addRoleToUser(user, ERole.ROLE_USER);
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

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
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
}
