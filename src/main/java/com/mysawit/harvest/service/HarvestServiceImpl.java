package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
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
    public HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId, UUID foremanId, String harvesterName) {
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
                .harvesterName(harvesterName)
                .weight(request.getWeight())
                .news(request.getNews())
                .photos(request.getPhotos())
                .harvestDate(LocalDateTime.now())
                .status(HarvestStatus.PENDING)
                .build();

        Harvest harvestSaved = harvestRepository.save(harvest);

        return mapResponse(harvestSaved);
    }

    @Override
    public List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId, UUID foremanId) {
        if (harvesterId == null) {
            if (foremanId != null) {
                throw new UnauthorizedUserException("Only registered harvesters are permitted to view their own harvest history.");
            }
            throw new UnauthorizedUserException("Required identity to view harvest logs.");
        }

        List<Harvest> harvestList = harvestRepository.findAllByHarvesterIdAndDate(
                harvesterId,
                request.getStartDate(),
                request.getEndDate()
        );

        return harvestList.stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    public List<HarvestResponse> foremanViewHarvest(ForemanViewHarvestRequest request, UUID harvesterId, UUID foremanId) {
        if (foremanId == null) {
            if (harvesterId != null) {
                throw new UnauthorizedUserException("Only registered foremen are permitted to access.");
            }
            throw new UnauthorizedUserException("Required identity to view harvest logs.");
        }

        List<Harvest> harvestList = harvestRepository.findAllByHarvesterNameAndDate(
                foremanId,
                request.getHarvesterName(),
                request.getStartDate(),
                request.getEndDate()
        );

        return harvestList.stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    public HarvestResponse updateHarvestStatus(UpdateHarvestStatusRequest request, UUID foremanId) {
        return null;
    }

    private HarvestResponse mapResponse(Harvest harvest) {
        return HarvestResponse.builder()
                .id(harvest.getId())
                .plantationId(harvest.getPlantationId())
                .harvesterId(harvest.getHarvesterId())
                .foremanId(harvest.getForemanId())
                .harvesterName(harvest.getHarvesterName())
                .weight(harvest.getWeight())
                .news(harvest.getNews())
                .photos(harvest.getPhotos())
                .status(harvest.getStatus())
                .rejectionReason(harvest.getRejectionReason())
                .harvestDate(harvest.getHarvestDate())
                .statusUpdatedDate(harvest.getStatusUpdatedDate())
                .build();
    }

}