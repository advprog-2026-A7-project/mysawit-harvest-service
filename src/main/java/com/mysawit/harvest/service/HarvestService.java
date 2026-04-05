package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.ForemanViewHarvestRequest;

import java.util.List;
import java.util.UUID;

public interface HarvestService {
    HarvestResponse logHarvest(LogHarvestRequest request, UUID harvesterId, UUID foremanId);

    List<HarvestResponse> viewHarvest(ForemanViewHarvestRequest request, UUID harvesterId, UUID foremanId);

    // TODO: HarvestStatusServiceImpl == untuk mandor update status log harvest buruh jadi approve / reject
}