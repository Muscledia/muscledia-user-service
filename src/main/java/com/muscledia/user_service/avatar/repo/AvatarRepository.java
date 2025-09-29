package com.muscledia.user_service.avatar.repo;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    List<Avatar> findByUser_UserId(Long userId);

    Optional<Avatar> findByUser_UserIdAndAvatarId(Long userId, Long avatarId);

    Optional<Avatar> findByUser_UserIdAndAvatarType(Long userId, AvatarType avatarType);

}
