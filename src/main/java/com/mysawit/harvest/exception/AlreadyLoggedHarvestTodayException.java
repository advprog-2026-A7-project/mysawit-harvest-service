package com.mysawit.harvest.exception;

public class AlreadyLoggedHarvestTodayException extends RuntimeException {
    public AlreadyLoggedHarvestTodayException(String message) {
        super(message);
    }
}