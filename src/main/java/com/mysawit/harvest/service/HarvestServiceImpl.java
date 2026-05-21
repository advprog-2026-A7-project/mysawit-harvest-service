package com.mysawit.harvest.service;

import com.mysawit.harvest.adapter.PayrollAdapter;
import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.mapper.HarvestMapper;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import com.mysawit.harvest.dto.HarvesterContext;
import com.mysawit.harvest.service.HarvesterContextService;
import com.mysawit.harvest.service.state.HarvestState;
import com.mysawit.harvest.service.validation.*;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestServiceImpl implements HarvestService {
    private final HarvestMapper harvestMapper;
    private final HarvestRepository harvestRepository;
    private final HarvesterContextService harvesterContextService;
    private final PayrollAdapter payrollAdapter;
    private final HarvestValidationChain harvestValidationChain;

    @Override
    public HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId) {
        harvestValidationChain.validate(request, harvesterId);

        HarvesterContext ctx = harvesterContextService.resolve(harvesterId);
        Harvest harvest = createPendingHarvest(request, harvesterId, ctx);

        return harvestMapper.mapToResponse(harvestRepository.save(harvest));
    }

    private Harvest createPendingHarvest(LogHarvestRequest request, UUID harvesterId, HarvesterContext ctx) {
        return Harvest.builder()
                .plantationId(String.valueOf(request.getPlantationId()))
                .harvesterId(harvesterId)
                .foremanId(ctx.foremanId())
                .harvesterName(ctx.harvesterName())
                .weight(request.getWeight())
                .news(request.getNews())
                .photos(request.getPhotos())
                .harvestDate(LocalDateTime.now())
                .status(HarvestStatus.PENDING)
                .build();
    }

    @Override
    public List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        List<Harvest> harvestList = harvestRepository.findAllByHarvesterIdAndDateAndStatus(
                harvesterId,
                request.getStatus(),
                request.getStartDate(),
                request.getEndDate()
        );

        return harvestList.stream()
                .map(harvestMapper::mapToResponse)
                .toList();
    }

    @Override
    public List<HarvestResponse> foremanViewHarvest(ForemanViewHarvestRequest request, UUID foremanId) {
        List<Harvest> harvestList = harvestRepository.findAllByHarvesterNameAndDate(
                foremanId,
                request.getHarvesterName(),
                request.getDate()
        );

        return harvestList.stream()
                .map(harvestMapper::mapToResponse)
                .toList();
    }

    @Override
    public HarvestResponse getHarvestDetail(UUID id, UUID harvesterId, UUID foremanId) {
        Harvest harvest = harvestRepository.findById(id)
                .orElseThrow(() -> new HarvestLogNotFoundException("Harvest log not found with ID: " + id));

        validateAuthorizedToGetHarvestDetail(harvest, harvesterId, foremanId);

        return harvestMapper.mapToResponse(harvest);
    }

    private void validateAuthorizedToGetHarvestDetail(Harvest harvest, UUID harvesterId, UUID foremanId) {
        if (foremanId != null && !harvest.getForemanId().equals(foremanId)) {
            throw new UnauthorizedUserException("You are not authorized to view this log (Foreman mismatch).");
        }

        if (foremanId == null && !harvest.getHarvesterId().equals(harvesterId)) {
            throw new UnauthorizedUserException("You are not authorized to view this log (Harvester mismatch).");
        }
    }

    @Override
    public HarvestResponse updateHarvestStatus(UpdateHarvestStatusRequest request, UUID foremanId) {
        Harvest harvest = harvestRepository.findById(request.getId())
                .orElseThrow(() -> new HarvestLogNotFoundException("Harvest log not found with ID: " + request.getId()));

        validateAuthorizedToUpdateHarvestStatus(harvest, foremanId);

        HarvestState state = HarvestState.of(harvest);

        Harvest updatedHarvest;
        if (request.getStatus() == HarvestStatus.APPROVED) {
            updatedHarvest = state.approve(harvest, request.getRejectionReason());
        } else if (request.getStatus() == HarvestStatus.REJECTED) {
            updatedHarvest = state.reject(harvest, request.getRejectionReason());
        } else {
            throw new IllegalArgumentException("Harvest status can only be updated to APPROVED or REJECTED status.");
        }

        Harvest savedHarvest = harvestRepository.save(updatedHarvest);
        if (savedHarvest.getStatus() == HarvestStatus.APPROVED) {
            payrollAdapter.publishApprovedHarvest(savedHarvest);
        }

        return harvestMapper.mapToResponse(savedHarvest);
    }

    private void validateAuthorizedToUpdateHarvestStatus(Harvest harvest, UUID foremanId) {
        if (!harvest.getForemanId().equals(foremanId)) {
            throw new UnauthorizedUserException("You are not authorized to update this log.");
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End Date must not be before Start Date");
        }
    }
}