package com.mysawit.harvest.service.state;

import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.model.Harvest;

public class RejectedState implements HarvestState {
    @Override
    public Harvest approve(Harvest harvest, String rejectionReason) {
        throw new HarvestStatusAlreadyUpdatedException("Already rejected, cannot approve.");
    }

    @Override
    public Harvest reject(Harvest harvest, String rejectionReason) {
        throw new HarvestStatusAlreadyUpdatedException("Already rejected.");
    }
}