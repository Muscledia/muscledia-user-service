package com.muscledia.user_service.avatar.service;

import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import com.muscledia.user_service.avatar.repo.AvatarRepository;
import com.muscledia.user_service.user.entity.User;
import com.muscledia.user_service.user.services.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AvatarServiceImpl implements IAvatarService {

    private final AvatarRepository avatarRepository;
    private final IUserService userService;

    /**
     * SIMPLE: Create a default avatar for new users
     *
     * Call this from UserController after user registration
     */
    @Transactional
    public Avatar createDefaultAvatarForNewUser(Long userId) {
        log.info("Creating default avatar for user: {}", userId);

        // Business logic: New users get ELF avatar by default
        // This can be easily changed or made configurable later
        AvatarType defaultType = AvatarType.ELF;

        return createAvatar(userId, defaultType);
    }

    /**
     * FUTURE: Allow users to choose avatar type after registration
     * This method can be called from a separate avatar selection endpoint
     */
    @Transactional
    public Avatar createChosenAvatarForUser(Long userId, AvatarType chosenType) {
        log.info("Creating chosen avatar {} for user: {}", chosenType, userId);

        return createAvatar(userId, chosenType);
    }


    @Override
    @Transactional
    public Avatar createAvatar(Long userId, AvatarType avatarType) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Avatar avatar = new Avatar();
        avatar.setUser(user);
        avatar.setAvatarType(avatarType);
        return avatarRepository.save(avatar);
    }

    @Override
    public Optional<Avatar> getAvatarById(Long avatarId) {
        return avatarRepository.findById(avatarId);
    }

    @Override
    public Optional<Avatar> getAvatarByUserId(Long userId) {
        List<Avatar> avatars = avatarRepository.findByUser_UserId(userId);
        // Logic to determine the "primary" avatar if a user has multiple.
        // For now, we'll just return the first one, but you might need a flag.
        return avatars.isEmpty() ? Optional.empty() : Optional.of(avatars.getFirst());
    }

    @Override
    public List<Avatar> getAllAvatarsForUser(Long userId) {
        return avatarRepository.findByUser_UserId(userId);
    }

    @Override
    public Optional<Avatar> getAvatarByUserIdAndAvatarId(Long userId, Long avatarId) {
        return avatarRepository.findByUser_UserIdAndAvatarId(userId, avatarId);
    }

    @Override
    public Optional<Avatar> getAvatarByUserIdAndType(Long userId, AvatarType avatarType) {
        return avatarRepository.findByUser_UserIdAndAvatarType(userId, avatarType);
    }

    @Override
    @Transactional
    public Avatar updateAvatarLevel(Long avatarId, int newLevel) {
        Avatar avatar = getAvatarById(avatarId)
                .orElseThrow(() -> new RuntimeException("Avatar not found"));
        avatar.setCurrentLevel(newLevel);
        return avatarRepository.save(avatar);
    }

    @Override
    @Transactional
    public Avatar updateAvatarExp(Long avatarId, int newExp) {
        Avatar avatar = getAvatarById(avatarId)
                .orElseThrow(() -> new RuntimeException("Avatar not found"));
        avatar.setCurrentExp(newExp);
        return avatarRepository.save(avatar);
    }

    @Override
    @Transactional
    public Avatar unlockAbility(Long avatarId, String abilityKey, Object abilityValue) {
        Avatar avatar = getAvatarById(avatarId)
                .orElseThrow(() -> new RuntimeException("Avatar not found"));


        Map<String, Object> abilities =  avatar.getUnlockedAbilities();
        if(abilities == null) {
            abilities = new HashMap<>();
        }
        abilities.put(abilityKey, abilityValue);
        avatar.setUnlockedAbilities(abilities);
        return avatarRepository.save(avatar);
    }

    @Override
    @Transactional
    public Avatar setFlameAnimation(Long avatarId, boolean enabled) {
        Avatar avatar = getAvatarById(avatarId)
                .orElseThrow(() -> new RuntimeException("Avatar not found"));
        avatar.setFlameAnimation(enabled);
        return avatarRepository.save(avatar);
    }

    @Override
    @Transactional
    public void deleteAvatar(Long avatarId) {
        avatarRepository.deleteById(avatarId);
    }
}
