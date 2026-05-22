package com.mysawit.harvest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LogHarvestRequestTest {
    private Validator validator;
    private final String plantationId = "PLT-12345678";

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private boolean hasViolationOnField(Set<ConstraintViolation<LogHarvestRequest>> violations, String fieldName) {
        for (ConstraintViolation<LogHarvestRequest> violation : violations) {
            if (violation.getPropertyPath().toString().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void validHarvest() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(plantationId);
        request.setWeight(777.0);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullPlantationId() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(null);
        request.setWeight(777.0);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "plantationId"));
    }

    @Test
    void nullWeight() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(plantationId);
        request.setWeight(null);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "weight"));
    }

    @Test
    void negativeWeight() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(plantationId);
        request.setWeight(-777.0);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "weight"));
    }

    @Test
    void zeroWeight() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(plantationId);
        request.setWeight(0.0);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "weight"));
    }

    @Test
    void nullNews() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(plantationId);
        request.setWeight(777.0);
        request.setNews(null);

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "news"));
    }

    @Test
    void nullPhotos() {
        LogHarvestRequest request = new LogHarvestRequest();
        request.setPlantationId(plantationId);
        request.setWeight(777.0);
        request.setNews("Successful harvest");
        request.setPhotos(null);

        Set<ConstraintViolation<LogHarvestRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}