package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.ViewHarvestRequest;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestServiceImpl implements HarvestService {
    private final HarvestRepository harvestRepository;

    @Override
    public HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId, UUID foremanId) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);

        boolean alreadyLoggedToday = harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
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

        Harvest harvestSaved = harvestRepository.save(harvest);

        return HarvestResponse.builder()
                .id(harvestSaved.getId())
                .plantationId(harvestSaved.getPlantationId())
                .harvesterId(harvestSaved.getHarvesterId())
                .foremanId(harvestSaved.getForemanId())
                .weight(harvestSaved.getWeight())
                .news(harvestSaved.getNews())
                .photos(harvestSaved.getPhotos())
                .status(harvestSaved.getStatus())
                .rejectionReason(harvestSaved.getRejectionReason())
                .harvestDate(harvestSaved.getHarvestDate())
                .statusUpdatedDate(harvestSaved.getStatusUpdatedDate())
                .build();
    }

    @Override
    public List<HarvestResponse> viewHarvest(ViewHarvestRequest request, UUID harvesterId, UUID foremanId) {
        return null;
    }
}