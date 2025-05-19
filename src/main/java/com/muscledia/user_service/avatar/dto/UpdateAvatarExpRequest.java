package com.muscledia.user_service.avatar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAvatarExpRequest {
    @NotNull(message = "New experience points are required")
    @Min(value = 0, message = "Experience must be non-negative")
    private int newExp;
}
