package com.mysawit.harvest.service.state;

import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RejectedStateTest {

    private RejectedState state;

    @BeforeEach
    void setUp() {
        state = new RejectedState();
    }

    @Test
    void approve_throwsException_whenAlreadyRejected() {
        assertThrows(HarvestStatusAlreadyUpdatedException.class,
                () -> state.approve(rejectedHarvest(), null));
    }

    @Test
    void reject_throwsException_whenAlreadyRejected() {
        assertThrows(HarvestStatusAlreadyUpdatedException.class,
                () -> state.reject(rejectedHarvest(), "Bad harvest"));
    }

    private Harvest rejectedHarvest() {
        return Harvest.builder()
                .id(UUID.randomUUID())
                .status(HarvestStatus.REJECTED)
                .build();
    }
}