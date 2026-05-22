package com.mysawit.harvest.service.state;

import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PendingStateTest {
    private PendingState state;

    @BeforeEach
    void setUp() {
        state = new PendingState();
    }

    @Test
    void approve_setsStatusToApprovedAndClearsRejectionReason() {
        Harvest harvest = pendingHarvest();
        harvest.setRejectionReason("old reason");

        Harvest result = state.approve(harvest, null);

        assertSame(harvest, result);
        assertEquals(HarvestStatus.APPROVED, harvest.getStatus());
        assertNull(harvest.getRejectionReason());
        assertNotNull(harvest.getStatusUpdatedDate());
    }

    @Test
    void approve_allowsBlankRejectionReason() {
        Harvest harvest = pendingHarvest();

        Harvest result = state.approve(harvest, "   ");

        assertSame(harvest, result);
        assertEquals(HarvestStatus.APPROVED, harvest.getStatus());
        assertNull(harvest.getRejectionReason());
        assertNotNull(harvest.getStatusUpdatedDate());
    }

    @Test
    void approve_throwsException_whenRejectionReasonIsProvided() {
        Harvest harvest = pendingHarvest();

        assertThrows(IllegalArgumentException.class,
                () -> state.approve(harvest, "Bad harvest"));
    }

    @Test
    void reject_setsStatusToRejectedAndStoresReason() {
        Harvest harvest = pendingHarvest();

        Harvest result = state.reject(harvest, "Bad harvest");

        assertSame(harvest, result);
        assertEquals(HarvestStatus.REJECTED, harvest.getStatus());
        assertEquals("Bad harvest", harvest.getRejectionReason());
        assertNotNull(harvest.getStatusUpdatedDate());
    }

    @Test
    void reject_throwsException_whenReasonIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> state.reject(pendingHarvest(), null));
    }

    @Test
    void reject_throwsException_whenReasonIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> state.reject(pendingHarvest(), "   "));
    }

    private Harvest pendingHarvest() {
        return Harvest.builder()
                .id(UUID.randomUUID())
                .status(HarvestStatus.PENDING)
                .build();
    }
}
