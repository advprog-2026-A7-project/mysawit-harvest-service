package com.mysawit.harvest.service;

import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.config.RabbitMQConfig;
import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestServiceImpl implements HarvestService {
    private final HarvestRepository harvestRepository;
    private final RabbitTemplate rabbitTemplate;
    private final IdentityClient identityClient;

    @Override
    public HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId) {
        IdentityUserResponse harvester = identityClient.getUserById(harvesterId);
        UUID assignedForemanId = identityClient.getAssignedForemanId(harvesterId);

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
                .foremanId(assignedForemanId)
                .harvesterName(harvester.getName())
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
    public List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId) {
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
    public List<HarvestResponse> foremanViewHarvest(ForemanViewHarvestRequest request, UUID foremanId) {
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
    public HarvestResponse getHarvestDetail(UUID id, UUID harvesterId, UUID foremanId) {
        Harvest harvest = harvestRepository.findById(id)
                .orElseThrow(() -> new HarvestLogNotFoundException("Harvest log not found with ID: " + id));

        if (foremanId != null) {
            if (!harvest.getForemanId().equals(foremanId)) {
                throw new UnauthorizedUserException("You are not authorized to view this log (Foreman mismatch).");
            }
        } else {
            if (!harvest.getHarvesterId().equals(harvesterId)) {
                throw new UnauthorizedUserException("You are not authorized to view this log (Harvester mismatch).");
            }
        }

        return mapResponse(harvest);
    }

    @Override
    public HarvestResponse updateHarvestStatus(UpdateHarvestStatusRequest request, UUID foremanId) {
        Harvest harvest = harvestRepository.findById(request.getId())
                .orElseThrow(() -> new HarvestLogNotFoundException("Harvest log not found with ID: " + request.getId()));

        if (!harvest.getForemanId().equals(foremanId)) {
            throw new UnauthorizedUserException("You are not authorized to update this log.");
        }

        if (harvest.getStatus() != HarvestStatus.PENDING) {
            throw new HarvestStatusAlreadyUpdatedException("Status already processed and cannot be changed.");
        }

        boolean isRejecting = request.getStatus() == HarvestStatus.REJECTED;
        boolean isApproving = request.getStatus() == HarvestStatus.APPROVED;

        if (isRejecting && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason must be provided when rejecting a harvest.");
        }

        if (isApproving && request.getRejectionReason() != null && !request.getRejectionReason().isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be provided for an approved harvest.");
        }

        harvest.setStatus(request.getStatus());
        harvest.setRejectionReason(isRejecting ? request.getRejectionReason() : null);
        harvest.setStatusUpdatedDate(LocalDateTime.now());

        Harvest savedHarvest = harvestRepository.save(harvest);

        if (isApproving) {
            sendToPayrollQueue(savedHarvest);
        }

        return mapResponse(savedHarvest);
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

    private void sendToPayrollQueue(Harvest harvest) {
        Map<String, Object> payrollInfo = Map.of(
                "harvestId", harvest.getId(),
                "harvesterId", harvest.getHarvesterId(),
                "weight", harvest.getWeight(),
                "status", harvest.getStatus().name()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYROLL_QUEUE, payrollInfo);
    }
}