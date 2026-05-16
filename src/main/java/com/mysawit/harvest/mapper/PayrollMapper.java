package com.mysawit.harvest.mapper;

import com.mysawit.harvest.dto.PayrollPayload;
import com.mysawit.harvest.model.Harvest;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {
    public PayrollPayload mapToPayload(Harvest harvest) {
        return PayrollPayload.builder()
                .harvestId(harvest.getId())
                .harvesterId(harvest.getHarvesterId())
                .weight(harvest.getWeight())
                .status(harvest.getStatus().name())
                .build();
    }
}