package com.mysawit.harvest.exception;

public class HarvestStatusAlreadyUpdatedException extends RuntimeException {
    public HarvestStatusAlreadyUpdatedException(String message) {
        super(message);
    }
}