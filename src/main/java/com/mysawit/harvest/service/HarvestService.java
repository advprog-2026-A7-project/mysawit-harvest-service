package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.*;

import java.util.List;
import java.util.UUID;

public interface HarvestService {
    HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId);

    List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId);

    List<HarvestResponse> foremanViewHarvest(ForemanViewHarvestRequest request, UUID foremanId);

    HarvestResponse getHarvestDetail(UUID id, UUID harvesterId, UUID foremanId);

    HarvestResponse updateHarvestStatus(UpdateHarvestStatusRequest request, UUID foremanId);
}