package com.muscledia.user_service.avatar.repo;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import com.muscledia.user_service.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    Optional<Avatar> findByUser(User user);

    boolean existsByUser(User user);

    Optional<Avatar> findByAvatarType(AvatarType avatarType);

}
