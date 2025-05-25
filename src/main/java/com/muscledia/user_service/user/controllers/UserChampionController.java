package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.user.dto.DefeatChampionRequest;
import com.muscledia.user_service.user.dto.StartBattleRequest;
import com.muscledia.user_service.user.dto.UpdateExerciseCountRequest;
import com.muscledia.user_service.user.entity.UserChampion;
import com.muscledia.user_service.exception.ResourceNotFoundException;
import com.muscledia.user_service.user.services.IUserChampionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-champions")
@Tag(name = "User Champions", description = "User champion management APIs")
@SecurityRequirement(name = "JWT")
public class UserChampionController {
    private final IUserChampionService userChampionService;

    public UserChampionController(@Qualifier("userChampionServiceImpl") IUserChampionService userChampionService) {
        this.userChampionService = userChampionService;
    }

    @Operation(summary = "Get user's champions", description = "Retrieves all champions associated with a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Champions retrieved successfully")
    })
    @GetMapping("users/{userId}")
    public ResponseEntity<List<UserChampion>> getUserChampions(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        List<UserChampion> champions = userChampionService.getUserChampionsByUserId(userId);
        return ResponseEntity.ok(champions);
    }

    @Operation(summary = "Get specific user champion", description = "Retrieves a specific champion for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Champion found"),
            @ApiResponse(responseCode = "404", description = "Champion not found for user")
    })
    @GetMapping("users/{userId}/{championId}")
    public ResponseEntity<UserChampion> getUserChampion(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @Parameter(description = "ID of the champion") @PathVariable Long championId) {
        return ResponseEntity.ok(userChampionService.getUserChampion(userId, championId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Champion %d not found for user %d", championId, userId))));
    }

    @Operation(summary = "Start champion battle", description = "Initiates a battle with a champion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Battle started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/start")
    public ResponseEntity<UserChampion> startBattle(@Valid @RequestBody StartBattleRequest startBattleRequest) {
        UserChampion startedBattle = userChampionService.startBattle(
                startBattleRequest.getUserId(),
                startBattleRequest.getChampionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(startedBattle);
    }

    @Operation(summary = "Update exercise count", description = "Updates the exercise count for a champion battle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercise count updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PatchMapping("/progress")
    public ResponseEntity<Void> updateExerciseCount(@Valid @RequestBody UpdateExerciseCountRequest updateRequest) {
        userChampionService.updateExerciseCount(
                updateRequest.getUserId(),
                updateRequest.getChampionId(),
                updateRequest.getCount());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark champion as defeated", description = "Marks a champion as defeated by the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Champion marked as defeated"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PatchMapping("/defeat")
    public ResponseEntity<Void> markChampionDefeated(@Valid @RequestBody DefeatChampionRequest defeatRequest) {
        userChampionService.markChampionDefeated(
                defeatRequest.getUserId(),
                defeatRequest.getChampionId());
        return ResponseEntity.noContent().build();
    }
}
