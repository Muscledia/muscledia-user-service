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
import java.util.Map;

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
            @ApiResponse(responseCode = "200", description = "Champions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
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

        UserChampion champion = userChampionService.getUserChampion(userId, championId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Champion %d not found for user %d", championId, userId)));

        return ResponseEntity.ok(champion);
    }

    @Operation(summary = "Start champion battle", description = "Initiates a battle with a champion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Battle started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/start")
    public ResponseEntity<?> startBattle(@Valid @RequestBody StartBattleRequest startBattleRequest) {
        try {
            UserChampion startedBattle = userChampionService.startBattle(
                    startBattleRequest.getUserId(),
                    startBattleRequest.getChampionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(startedBattle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Update exercise count", description = "Updates the exercise count for a champion battle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercise count updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Champion battle not found")
    })
    @PatchMapping("/progress")
    public ResponseEntity<?> updateExerciseCount(@Valid @RequestBody UpdateExerciseCountRequest updateRequest) {
        try {
            if (updateRequest.getCount() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Exercise count cannot be negative"));
            }

            userChampionService.updateExerciseCount(
                    updateRequest.getUserId(),
                    updateRequest.getChampionId(),
                    updateRequest.getCount());

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Mark champion as defeated", description = "Marks a champion as defeated by the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Champion marked as defeated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Champion battle not found"),
            @ApiResponse(responseCode = "409", description = "Champion already defeated")
    })
    @PatchMapping("/defeat")
    public ResponseEntity<?> markChampionDefeated(@Valid @RequestBody DefeatChampionRequest defeatRequest) {
        try {
            userChampionService.markChampionDefeated(
                    defeatRequest.getUserId(),
                    defeatRequest.getChampionId());

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

