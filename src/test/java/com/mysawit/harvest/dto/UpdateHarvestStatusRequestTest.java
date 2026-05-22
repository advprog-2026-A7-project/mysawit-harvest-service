package com.mysawit.harvest.dto;

import com.mysawit.harvest.model.HarvestStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UpdateHarvestStatusRequestTest {
    private Validator validator;
    private UpdateHarvestStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        updateStatusRequest = new UpdateHarvestStatusRequest();
    }

    @Test
    void testDtoGetterSetterAndBuilder() {
        UUID id = UUID.randomUUID();
        String reason = "Bad harvest.";

        updateStatusRequest = UpdateHarvestStatusRequest.builder()
                .id(id)
                .status(HarvestStatus.REJECTED)
                .rejectionReason(reason)
                .build();

        assertEquals(id, updateStatusRequest.getId());
        assertEquals(HarvestStatus.REJECTED, updateStatusRequest.getStatus());
        assertEquals(reason, updateStatusRequest.getRejectionReason());

        updateStatusRequest.setStatus(HarvestStatus.APPROVED);
        assertEquals(HarvestStatus.APPROVED, updateStatusRequest.getStatus());
    }

    @Test
    void testValidation_Success() {
        updateStatusRequest.setId(UUID.randomUUID());
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);

        Set<ConstraintViolation<UpdateHarvestStatusRequest>> violations = validator.validate(updateStatusRequest);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_FailedWhenIdIsNull() {
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);

        Set<ConstraintViolation<UpdateHarvestStatusRequest>> violations = validator.validate(updateStatusRequest);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Harvest log ID is required", violations.iterator().next().getMessage());
    }
}