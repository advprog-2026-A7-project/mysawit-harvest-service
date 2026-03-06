package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestLogServiceImpl implements HarvestService {

    private final HarvestRepository harvestRepository;

    @Override
    public HarvestResponse logHarvest(HarvestRequest request, UUID harvesterId, UUID foremanId) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);

        boolean alreadyLoggedToday = harvestRepository.existsTodaysHarvestByHarvesterId(
                harvesterId, dayStart, dayEnd
        );

        if (alreadyLoggedToday) {
            throw new AlreadyLoggedHarvestTodayException(
                    "You have already logged a harvest today. Please try again tomorrow."
            );
        }

        Harvest harvest = Harvest.builder()
                .plantationId(request.getPlantationId())
                .harvesterId(harvesterId)
                .foremanId(foremanId)
                .weight(request.getWeight())
                .news(request.getNews())
                .photos(request.getPhotos())
                .status(HarvestStatus.PENDING)
                .build();

        Harvest saved = harvestRepository.save(harvest);

        return HarvestResponse.builder()
                .id(saved.getId())
                .plantationId(saved.getPlantationId())
                .harvesterId(saved.getHarvesterId())
                .foremanId(saved.getForemanId())
                .weight(saved.getWeight())
                .news(saved.getNews())
                .photos(saved.getPhotos())
                .status(saved.getStatus())
                .rejectionReason(saved.getRejectionReason())
                .harvestDate(saved.getHarvestDate())
                .statusUpdatedDate(saved.getStatusUpdatedDate())
                .build();
    }
}