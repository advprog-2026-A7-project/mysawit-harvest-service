package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

public interface HarvestService {
    HarvestResponse logHarvest(HarvestRequest request, UUID harvesterId, UUID foremanId);

    List<HarvestResponse> viewHarvest(HarvestRequest request, UUID harvesterId, UUID foremanId);

    // TODO: HarvestStatusServiceImpl == untuk mandor update status log harvest buruh jadi approve / reject
}