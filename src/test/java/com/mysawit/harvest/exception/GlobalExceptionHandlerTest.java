package com.mysawit.harvest.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpInputMessage;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private HttpInputMessage mockInputMessage;
    private GlobalExceptionHandler exceptionHandler;

    enum DummyEnum { YES, NO }

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidationError_WithFieldError_Normal() {
        BindException ex = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "status", "Status cannot be empty");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ResponseEntity<?> response = exceptionHandler.handleValidationError(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("VALIDATION_ERROR", body.get("error"));
        assertEquals("Status cannot be empty", body.get("message"));
    }

    @Test
    void testHandleValidationError_WithFieldError_TypeMismatch() {
        BindException ex = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "weight", null, false, new String[]{"typeMismatch.weight"}, null, "Invalid type");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ResponseEntity<?> response = exceptionHandler.handleValidationError(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("VALIDATION_ERROR", body.get("error"));
        assertEquals("Invalid value provided for field 'weight'.", body.get("message"));
    }

    @Test
    void testHandleValidationError_NullFieldError() {
        BindException ex = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(null);

        ResponseEntity<?> response = exceptionHandler.handleValidationError(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("VALIDATION_ERROR", body.get("error"));
        assertEquals("Validation error", body.get("message"));
    }

    @Test
    void testHandleTypeMismatch() {
        MethodParameter parameter = mock(MethodParameter.class);

        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", parameter, new RuntimeException()
        );

        ResponseEntity<?> response = exceptionHandler.handleTypeMismatch(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("INVALID_PARAMETER", body.get("error"));
        assertEquals("Invalid value 'abc' for parameter 'id'.", body.get("message"));
    }

    @Test
    void testHandleHttpMessageNotReadable_GenericCause() {
        mockInputMessage = mock(HttpInputMessage.class);

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON request or invalid data type provided.", new RuntimeException("Generic JSON error"), mockInputMessage);

        ResponseEntity<?> response = exceptionHandler.handleHttpMessageNotReadable(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("INVALID_REQUEST_BODY", body.get("error"));
        assertEquals("Malformed JSON request or invalid data type provided.", body.get("message"));
    }

    @Test
    void testHandleHttpMessageNotReadable_InvalidFormatException_Enum() {
        InvalidFormatException mockFormatEx = mock(InvalidFormatException.class);

        Class<?> targetType = DummyEnum.class;
        when(mockFormatEx.getTargetType()).thenAnswer(invocation -> targetType);

        com.fasterxml.jackson.databind.JsonMappingException.Reference reference =
                new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "status");
        when(mockFormatEx.getPath()).thenReturn(List.of(reference));

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid format", mockFormatEx, mockInputMessage);


        ResponseEntity<?> response = exceptionHandler.handleHttpMessageNotReadable(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("INVALID_REQUEST_BODY", body.get("error"));
        assertEquals("Invalid value provided for field 'status'.", body.get("message"));
    }

    @Test
    void testHandleHttpMessageNotReadable_InvalidFormatException_TargetTypeNull() {
        InvalidFormatException mockFormatEx = mock(InvalidFormatException.class);
        when(mockFormatEx.getTargetType()).thenReturn(null);

        com.fasterxml.jackson.databind.JsonMappingException.Reference reference =
                new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "age");
        when(mockFormatEx.getPath()).thenReturn(java.util.List.of(reference));

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid format", mockFormatEx, mockInputMessage);

        ResponseEntity<?> response = exceptionHandler.handleHttpMessageNotReadable(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON request or invalid data type provided.", body.get("message"));
    }

    @Test
    void testHandleHttpMessageNotReadable_InvalidFormatException_NotAnEnum() {
        InvalidFormatException mockFormatEx = mock(InvalidFormatException.class);

        Class<?> targetType = Integer.class;
        when(mockFormatEx.getTargetType()).thenAnswer(invocation -> targetType);

        com.fasterxml.jackson.databind.JsonMappingException.Reference reference =
                new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "age");
        when(mockFormatEx.getPath()).thenReturn(java.util.List.of(reference));

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid format", mockFormatEx, mockInputMessage);

        ResponseEntity<?> response = exceptionHandler.handleHttpMessageNotReadable(ex);
        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON request or invalid data type provided.", body.get("message"));
    }
}