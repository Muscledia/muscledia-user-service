package com.muscledia.user_service.user.exception;

public class BadgeAlreadyAwardedException extends RuntimeException {
    public BadgeAlreadyAwardedException(String message) {
        super(message);
    }
}