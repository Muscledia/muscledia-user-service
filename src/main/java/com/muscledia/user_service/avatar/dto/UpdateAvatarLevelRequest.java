package com.muscledia.user_service.avatar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAvatarLevelRequest {
    @NotNull(message = "New level is required")
    @Min(value = 1, message = "Level must be at least 1")
    private int newLevel;
}
