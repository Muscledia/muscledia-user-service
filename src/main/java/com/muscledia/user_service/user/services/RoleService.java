package com.muscledia.user_service.user.services;

import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.Role;
import com.muscledia.user_service.user.entity.User;

public interface RoleService {
    Role findByName(ERole name);

    void addRoleToUser(User user, ERole roleName);

    void removeRoleFromUser(User user, ERole roleName);

    boolean hasRole(User user, ERole roleName);
}