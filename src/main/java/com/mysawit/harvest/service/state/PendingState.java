package com.mysawit.harvest.service.state;

import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;

import java.time.LocalDateTime;

public class PendingState implements HarvestState {
    @Override
    public Harvest approve(Harvest harvest, String rejectionReason) {
        if (!isBlank(rejectionReason)) {
            throw new IllegalArgumentException("Rejection reason cannot be provided for an approved harvest.");
        }

        harvest.setStatus(HarvestStatus.APPROVED);
        harvest.setRejectionReason(null);
        harvest.setStatusUpdatedDate(LocalDateTime.now());
        return harvest;
    }

    @Override
    public Harvest reject(Harvest harvest, String rejectionReason) {
        if (isBlank(rejectionReason)) {
            throw new IllegalArgumentException("Rejection reason required.");
        }

        harvest.setStatus(HarvestStatus.REJECTED);
        harvest.setRejectionReason(rejectionReason);
        harvest.setStatusUpdatedDate(LocalDateTime.now());
        return harvest;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
