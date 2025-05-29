package com.muscledia.user_service.config;

import com.muscledia.user_service.user.entity.ERole;
import com.muscledia.user_service.user.entity.Role;
import com.muscledia.user_service.user.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Initialize roles if they don't exist
        for (ERole role : ERole.values()) {
            if (!roleRepository.findByName(role).isPresent()) {
                Role newRole = new Role(role);
                roleRepository.save(newRole);
            }
        }
    }
}