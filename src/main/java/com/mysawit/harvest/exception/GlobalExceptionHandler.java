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

    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<?> handleValidationError(org.springframework.validation.BindException ex) {
        org.springframework.validation.FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation error";

        if (fieldError != null && fieldError.getCode() != null && fieldError.getCode().contains("typeMismatch")) {
            message = "Invalid value provided for field '" + fieldError.getField() + "'.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", message
        ));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "INVALID_PARAMETER",
                "message", "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'."
        ));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request or invalid data type provided.";

        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException formatEx) {
            if (formatEx.getTargetType() != null && formatEx.getTargetType().isEnum()) {
                message = "Invalid value provided for field '" + formatEx.getPath().getFirst().getFieldName() + "'.";
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "INVALID_REQUEST_BODY",
                "message", message
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