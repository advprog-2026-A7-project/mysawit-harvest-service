package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import java.util.UUID;

public abstract class HarvestValidationHandler {
    private HarvestValidationHandler next;

    public HarvestValidationHandler setNext(HarvestValidationHandler next) {
        this.next = next;
        return next;
    }

    public final void handle(LogHarvestRequest request, UUID harvesterId) {
        validate(request, harvesterId);
        if (next != null) {
            next.handle(request, harvesterId);
        }
    }

    protected abstract void validate(LogHarvestRequest request, UUID harvesterId);
}