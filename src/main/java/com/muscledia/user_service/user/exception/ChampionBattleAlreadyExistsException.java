package com.muscledia.user_service.user.exception;

public class ChampionBattleAlreadyExistsException extends RuntimeException {
    public ChampionBattleAlreadyExistsException(String message) {
        super(message);
    }
}