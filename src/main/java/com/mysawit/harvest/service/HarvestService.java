package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.ForemanViewHarvestRequest;
import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.HarvesterViewHarvestRequest;

import java.util.List;
import java.util.UUID;

public interface HarvestService {
    HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId, UUID foremanId, String harvesterName);

    List<HarvestResponse> harvesterViewHarvest(HarvesterViewHarvestRequest request, UUID harvesterId, UUID foremanId);

    List<HarvestResponse> foremanViewHarvest(ForemanViewHarvestRequest request, UUID harvesterId, UUID foremanId);

    // TODO: HarvestStatusServiceImpl == untuk mandor update status log harvest buruh jadi approve / reject
}