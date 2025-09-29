package com.muscledia.user_service.avatar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetFlameAnimationRequest {
    @NotNull(message = "Enabled status is required")
    private boolean enabled;
}
