package com.mysawit.harvest.proxy;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.service.HarvestService;
import com.mysawit.harvest.service.HarvestServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class HarvestServiceProxy implements HarvestService {

    private final HarvestServiceImpl harvestService;

    @Override
    public HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId) {
        if (harvesterId == null) {
            throw new UnauthorizedUserException("Access Denied: Only harvesters are permitted to log harvest data.");
        }
        return harvestService.logHarvest(request, harvesterId);
    }

    @Override
    public List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId) {
        if (harvesterId == null) {
            throw new UnauthorizedUserException("Access Denied: Harvester identity is required to view personal history.");
        }
        return harvestService.harvesterViewHarvest(request, harvesterId);
    }

    @Override
    public List<HarvestResponse> foremanViewHarvest(ForemanViewHarvestRequest request, UUID foremanId) {
        if (foremanId == null) {
            throw new UnauthorizedUserException("Access Denied: Only foremen are permitted to access team harvest history.");
        }
        return harvestService.foremanViewHarvest(request, foremanId);
    }

    @Override
    public HarvestResponse getHarvestDetail(UUID id, UUID harvesterId, UUID foremanId) {
        if (harvesterId == null && foremanId == null) {
            throw new UnauthorizedUserException("Access Denied: Authentication identity is required to view details.");
        }
        return harvestService.getHarvestDetail(id, harvesterId, foremanId);
    }

    @Override
    public HarvestResponse updateHarvestStatus(UpdateHarvestStatusRequest request, UUID foremanId) {
        if (foremanId == null) {
            throw new UnauthorizedUserException("Access Denied: Only assigned foremen are permitted to update harvest status.");
        }
        return harvestService.updateHarvestStatus(request, foremanId);
    }
}