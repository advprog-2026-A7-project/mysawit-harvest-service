package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.repository.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestLogServiceImpl implements HarvestService {

    private final HarvestRepository harvestRepository;

    @Override
    public HarvestResponse logHarvest(HarvestRequest request, UUID harvesterId, UUID foremanId) {
        // TODO: implement setelah TDD done!!!!
        return null;
    }
}