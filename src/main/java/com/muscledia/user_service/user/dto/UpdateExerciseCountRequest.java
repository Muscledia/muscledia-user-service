package com.muscledia.user_service.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateExerciseCountRequest {
    @NotNull(message = "userId is required")
    private Long userId;
    @NotNull(message = "championId is required")
    private Long championId;
    @NotNull(message = "count is required")
    private Integer count;
}
