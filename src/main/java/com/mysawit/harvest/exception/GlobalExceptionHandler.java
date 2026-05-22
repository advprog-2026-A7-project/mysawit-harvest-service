package com.mysawit.harvest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";


    @ExceptionHandler(AlreadyLoggedHarvestTodayException.class)
    public ResponseEntity<?> handleAlreadyLogged(AlreadyLoggedHarvestTodayException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                ERROR_KEY, "ALREADY_LOGGED_TODAY",
                MESSAGE_KEY, ex.getMessage()
        ));
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<?> handleUnauthorizedUser(UnauthorizedUserException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                ERROR_KEY, "UNAUTHORIZED_ACCESS",
                MESSAGE_KEY, ex.getMessage()
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
                ERROR_KEY, "VALIDATION_ERROR",
                MESSAGE_KEY, message
        ));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                ERROR_KEY, "INVALID_PARAMETER",
                MESSAGE_KEY, "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'."
        ));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request or invalid data type provided.";

        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException formatEx
                && formatEx.getTargetType() != null
                && formatEx.getTargetType().isEnum()) {
            message = "Invalid value provided for field '" + formatEx.getPath().getFirst().getFieldName() + "'.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                ERROR_KEY, "INVALID_REQUEST_BODY",
                MESSAGE_KEY, message
        ));
    }

    @ExceptionHandler(HarvestStatusAlreadyUpdatedException.class)
    public ResponseEntity<?> handleStatusAlreadyUpdated(HarvestStatusAlreadyUpdatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                ERROR_KEY, "STATUS_ALREADY_UPDATED",
                MESSAGE_KEY, ex.getMessage()
        ));
    }

    @ExceptionHandler(HarvestLogNotFoundException.class)
    public ResponseEntity<?> handleNotFound(HarvestLogNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                ERROR_KEY, "NOT_FOUND",
                MESSAGE_KEY, ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                ERROR_KEY, "INVALID_ARGUMENT",
                MESSAGE_KEY, ex.getMessage()
        ));
    }
}