package com.mysawit.harvest.service.state;

import com.mysawit.harvest.model.Harvest;

public interface HarvestState {
    Harvest approve(Harvest harvest, String rejectionReason);
    Harvest reject(Harvest harvest, String rejectionReason);

    static HarvestState of(Harvest harvest) {
        return switch (harvest.getStatus()) {
            case PENDING -> new PendingState();
            case APPROVED -> new ApprovedState();
            case REJECTED -> new RejectedState();
        };
    }
}
