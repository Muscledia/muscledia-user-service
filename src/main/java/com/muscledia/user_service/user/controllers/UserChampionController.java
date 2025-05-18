package com.muscledia.user_service.user.controllers;

import com.muscledia.user_service.user.dto.DefeatChampionRequest;
import com.muscledia.user_service.user.dto.StartBattleRequest;
import com.muscledia.user_service.user.dto.UpdateExerciseCountRequest;
import com.muscledia.user_service.user.entity.UserChampion;
import com.muscledia.user_service.user.services.IUserChampionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-champions")
public class UserChampionController {
    private final IUserChampionService userChampionService;

    public UserChampionController(@Qualifier("userChampionServiceImpl") IUserChampionService userChampionService) {
        this.userChampionService = userChampionService;
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<List<UserChampion>> getUserChampions(@PathVariable Long userId) {
        return ResponseEntity.ok(userChampionService.getUserChampionsByUserId(userId));
    }

    @GetMapping("users/{userId}/{championId}")
    public ResponseEntity<Optional<UserChampion>> getUserChampion(@PathVariable Long userId, @PathVariable Long championId) {
        return ResponseEntity.ok(userChampionService.getUserChampion(userId, championId));
    }

    @PostMapping("/start")
    public ResponseEntity<UserChampion> startBattle(@Valid @RequestBody StartBattleRequest startBattleRequest) {
        UserChampion startedBattle = userChampionService.startBattle(
                startBattleRequest.getUserId(),
                startBattleRequest.getChampionId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(startedBattle);
    }

    @PatchMapping("/progress")
    public ResponseEntity<Void> updateExerciseCount(@Valid @RequestBody UpdateExerciseCountRequest updateRequest) {
        userChampionService.updateExerciseCount(
                updateRequest.getUserId(),
                updateRequest.getChampionId(),
                updateRequest.getCount()
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/defeat")
    public ResponseEntity<Void> markChampionDefeated(@Valid @RequestBody DefeatChampionRequest defeatRequest) {
        userChampionService.markChampionDefeated(
                defeatRequest.getUserId(),
                defeatRequest.getChampionId()
        );
        return ResponseEntity.noContent().build();
    }

}
