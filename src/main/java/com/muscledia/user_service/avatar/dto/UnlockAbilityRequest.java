package com.muscledia.user_service.avatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UnlockAbilityRequest {
    @NotBlank(message = "Ability key is required")
    private String abilityKey;
    @NotNull(message = "Ability value is required")
    private Object abilityValue;
}
