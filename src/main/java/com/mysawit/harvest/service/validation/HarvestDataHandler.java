package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.service.validation.HarvestValidationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HarvestDataHandler extends HarvestValidationHandler {
    @Override
    protected void validate(LogHarvestRequest request, UUID harvesterId) {
        if (request.getWeight() == null || request.getWeight() <= 0) {
            throw new IllegalArgumentException("Harvest weight must be greater than 0.");
        }

        if (request.getPhotos() == null || request.getPhotos().isEmpty()) {
            throw new IllegalArgumentException("At least one photo must be provided.");
        }

        if (request.getNews() == null || request.getNews().isBlank()) {
            throw new IllegalArgumentException("Harvest news must be provided.");
        }
    }
}