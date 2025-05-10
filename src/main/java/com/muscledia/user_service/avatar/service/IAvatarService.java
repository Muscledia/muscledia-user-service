package com.muscledia.user_service.avatar.service;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;

import java.util.List;
import java.util.Optional;

public interface IAvatarService {
    Avatar createAvatar(Long userId, AvatarType avatarType);

    Optional<Avatar> getAvatarById(Long avatarId);

    Optional<Avatar> getAvatarByUserId(Long userId); // Returns the primary avatar or handles logic

    List<Avatar> getAllAvatarsForUser(Long userId);

    Optional<Avatar> getAvatarByUserIdAndAvatarId(Long userId, Long avatarId);

    Optional<Avatar> getAvatarByUserIdAndType(Long userId, AvatarType avatarType);

    Avatar updateAvatarLevel(Long avatarId, int newLevel);

    Avatar updateAvatarExp(Long avatarId, int newExp);

    Avatar unlockAbility(Long avatarId, String abilityKey, Object abilityValue);

    Avatar setFlameAnimation(Long avatarId, boolean enabled);

    void deleteAvatar(Long avatarId);
}
