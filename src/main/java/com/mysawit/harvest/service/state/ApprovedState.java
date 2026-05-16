package com.mysawit.harvest.service.state;

import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.model.Harvest;

public class ApprovedState implements HarvestState {
    @Override
    public Harvest approve(Harvest harvest, String rejectionReason) {
        throw new HarvestStatusAlreadyUpdatedException("Already approved.");
    }

    @Override
    public Harvest reject(Harvest harvest, String rejectionReason) {
        throw new HarvestStatusAlreadyUpdatedException("Already approved, cannot reject.");
    }
}
