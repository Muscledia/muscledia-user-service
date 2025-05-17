package com.muscledia.user_service.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartBattleRequest {
    @NotNull(message = "userId is required")
    private Long userId;
    @NotNull(message = "championId is required")
    private Long championId;
}
