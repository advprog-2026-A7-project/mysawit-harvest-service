package com.mysawit.harvest.service.state;

import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HarvestStateTest {

    @Test
    void of_returnsPendingState_whenHarvestStatusIsPending() {
        Harvest harvest = Harvest.builder().status(HarvestStatus.PENDING).build();

        assertInstanceOf(PendingState.class, HarvestState.of(harvest));
    }

    @Test
    void of_returnsApprovedState_whenHarvestStatusIsApproved() {
        Harvest harvest = Harvest.builder().status(HarvestStatus.APPROVED).build();

        assertInstanceOf(ApprovedState.class, HarvestState.of(harvest));
    }

    @Test
    void of_returnsRejectedState_whenHarvestStatusIsRejected() {
        Harvest harvest = Harvest.builder().status(HarvestStatus.REJECTED).build();

        assertInstanceOf(RejectedState.class, HarvestState.of(harvest));
    }
}
