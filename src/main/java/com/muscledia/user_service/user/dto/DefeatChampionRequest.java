package com.muscledia.user_service.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DefeatChampionRequest {
    @NotNull(message = "UserId id required")
    private Long userId;
    @NotNull(message = "ChampionId id required")
    private Long championId;
}
