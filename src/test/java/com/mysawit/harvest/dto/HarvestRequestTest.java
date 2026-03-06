package com.mysawit.harvest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HarvestRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Helper method untuk cek validation pada suatu field
    private boolean hasViolationOnField(Set<ConstraintViolation<HarvestRequest>> violations, String fieldName) {
        for (ConstraintViolation<HarvestRequest> violation : violations) {
            if (violation.getPropertyPath().toString().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void validHarvest() { // SUCCESS
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(UUID.randomUUID());
        request.setWeight(300.5);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullPlantationId() { // FAIL
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(null);
        request.setWeight(300.5);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "plantationId"));
    }

    @Test
    void nullWeight() { // FAIL
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(UUID.randomUUID());
        request.setWeight(null);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "weight"));
    }

    @Test
    void negativeWeight() { // FAIL
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(UUID.randomUUID());
        request.setWeight(-300.5);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "weight"));
    }

    @Test
    void zeroWeight() { // FAIL
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(UUID.randomUUID());
        request.setWeight(0.0);
        request.setNews("Successful harvest");

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "weight"));
    }

    @Test
    void nullNews() { // FAIL
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(UUID.randomUUID());
        request.setWeight(300.5);
        request.setNews(null);

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(hasViolationOnField(violations, "news"));
    }

    @Test
    void nullPhotos() { // SUCCESS
        HarvestRequest request = new HarvestRequest();
        request.setPlantationId(UUID.randomUUID());
        request.setWeight(300.5);
        request.setNews("Successful harvest");
        request.setPhotos(null);

        Set<ConstraintViolation<HarvestRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}