package com.muscledia.user_service.user.repo;

import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}