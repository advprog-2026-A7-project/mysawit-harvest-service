package com.mysawit.harvest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyLoggedHarvestTodayException.class)
    public ResponseEntity<?> handleAlreadyLogged(AlreadyLoggedHarvestTodayException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "ALREADY_LOGGED_TODAY",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<?> handleUnauthorizedUser(UnauthorizedUserException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "UNAUTHORIZED_ACCESS",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationError(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage()
        ));
    }

    @ExceptionHandler(HarvestStatusAlreadyUpdatedException.class)
    public ResponseEntity<?> handleStatusAlreadyUpdated(HarvestStatusAlreadyUpdatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "STATUS_ALREADY_UPDATED",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(HarvestLogNotFoundException.class)
    public ResponseEntity<?> handleNotFound(HarvestLogNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "INVALID_ARGUMENT",
                "message", ex.getMessage()
        ));
    }
}