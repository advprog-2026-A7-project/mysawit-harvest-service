package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.repository.HarvestRepository;
import com.mysawit.harvest.service.validation.HarvestValidationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlreadyLoggedTodayHandler extends HarvestValidationHandler {
    private final HarvestRepository harvestRepository;

    @Override
    protected void validate(LogHarvestRequest request, UUID harvesterId) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);

        boolean alreadyLogged = harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                harvesterId, dayStart, dayEnd
        );

        if (alreadyLogged) {
            throw new AlreadyLoggedHarvestTodayException(
                    "You have already logged a harvest today. Please try again tomorrow."
            );
        }
    }
}