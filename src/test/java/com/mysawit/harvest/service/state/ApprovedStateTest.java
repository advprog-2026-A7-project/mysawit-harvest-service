package com.mysawit.harvest.service.state;

import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovedStateTest {

    private ApprovedState state;

    @BeforeEach
    void setUp() {
        state = new ApprovedState();
    }

    @Test
    void approve_throwsException_whenAlreadyApproved() {
        assertThrows(HarvestStatusAlreadyUpdatedException.class,
                () -> state.approve(approvedHarvest(), null));
    }

    @Test
    void reject_throwsException_whenAlreadyApproved() {
        assertThrows(HarvestStatusAlreadyUpdatedException.class,
                () -> state.reject(approvedHarvest(), "Bad harvest"));
    }

    private Harvest approvedHarvest() {
        return Harvest.builder()
                .id(UUID.randomUUID())
                .status(HarvestStatus.APPROVED)
                .build();
    }
}
