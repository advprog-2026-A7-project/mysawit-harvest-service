package com.mysawit.harvest.service;

import com.mysawit.harvest.adapter.PayrollAdapter;
import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.mapper.HarvestMapper;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.HarvestRepository;
import com.mysawit.harvest.repository.UserReplicaRepository;

import com.mysawit.harvest.service.state.HarvestState;
import lombok.RequiredArgsConstructor;

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
    private static final String HARVESTER_ROLE = "BURUH";

    private final HarvestMapper harvestMapper;
    private final HarvestRepository harvestRepository;
    private final IdentityClient identityClient;
    private final UserReplicaRepository userReplicaRepository;
    private final PayrollAdapter payrollAdapter;

    @Override
    public HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId) {
        validateHarvesterHasNotLoggedToday(harvesterId);

        HarvesterContext ctx = resolveHarvesterContext(harvesterId);
        Harvest harvest = createPendingHarvest(request, harvesterId, ctx);
        Harvest harvestSaved = harvestRepository.save(harvest);

        return harvestMapper.mapToResponse(harvestSaved);
    }

    private void validateHarvesterHasNotLoggedToday(UUID harvesterId) {
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
    }

    private Harvest createPendingHarvest(LogHarvestRequest request, UUID harvesterId, HarvesterContext ctx) {
        return Harvest.builder()
                .plantationId(request.getPlantationId())
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

    private HarvesterContext resolveHarvesterContext(UUID harvesterId) {
        UserReplica replica = userReplicaRepository.findById(harvesterId).orElse(null);

        if (replica != null
                && HARVESTER_ROLE.equals(replica.getRole())
                && replica.getMandorId() != null
                && replica.getName() != null) {
            return new HarvesterContext(replica.getName(), replica.getMandorId());
        }

        // Fallback while the replica is cold or incomplete.
        // Once user.registered + user.assigned have flowed for this user,
        // this branch stops firing and the sync HTTP call disappears.
        IdentityUserResponse harvester = identityClient.getUserById(harvesterId);
        UUID foremanId = identityClient.getAssignedForemanId(harvesterId);
        return new HarvesterContext(harvester.getName(), foremanId);
    }

    private record HarvesterContext(String harvesterName, UUID foremanId) {}

    @Override
    public List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId) {
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
                request.getStartDate(),
                request.getEndDate()
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
}