package com.muscledia.user_service.exception;

public class BadgeAlreadyAwardedException extends RuntimeException {
    public BadgeAlreadyAwardedException(String message) {
        super(message);
    }
}