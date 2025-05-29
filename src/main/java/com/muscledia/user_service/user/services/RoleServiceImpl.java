package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.Role;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.repo.RoleRepository;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role findByName(ERole name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
    }

    @Override
    @Transactional
    public void addRoleToUser(User user, ERole roleName) {
        Role role = findByName(roleName);
        user.addRole(role);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(User user, ERole roleName) {
        Role role = findByName(roleName);
        user.removeRole(role);
    }

    @Override
    public boolean hasRole(User user, ERole roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }
}