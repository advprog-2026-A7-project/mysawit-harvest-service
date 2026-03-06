package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;

import java.util.UUID;

public interface HarvestService {
    // HarvestLogServiceImpl
    HarvestResponse logHarvest(HarvestRequest request, UUID harvesterId, UUID foremanId);

    // TODO: HarvestListServiceImpl == untuk show list of harvest dari buruhnya
    // TODO: HarvestStatusServiceImpl == untuk mandor update status log harvest buruh jadi approve / reject
}