package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HarvestValidationChain {
    private final HarvestValidationHandler firstHandler;

    public HarvestValidationChain(
            AlreadyLoggedTodayHandler alreadyLoggedTodayHandler,
            HarvesterAssignedHandler harvesterAssignedHandler,
            HarvestDataHandler harvestDataHandler
    ) {
        alreadyLoggedTodayHandler
                .setNext(harvesterAssignedHandler)
                .setNext(harvestDataHandler);

        this.firstHandler = alreadyLoggedTodayHandler;
    }

    public void validate(LogHarvestRequest request, UUID harvesterId) {
        firstHandler.handle(request, harvesterId);
    }
}
